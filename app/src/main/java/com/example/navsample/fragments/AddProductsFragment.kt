package com.example.navsample.fragments


import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.ImageAnalyzer
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.ProductListAdapter
import com.example.navsample.databinding.FragmentAddProductsBinding
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.mlkit.vision.common.InputImage


@ExperimentalGetImage
class AddProductsFragment : Fragment(), ItemClickListener {

    private var _binding: FragmentAddProductsBinding? = null
    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductsBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun initObserver() {
        receiptImageViewModel.bitmapCropped.observe(viewLifecycleOwner) {
            it?.let {
                if (receiptImageViewModel.bitmapCropped.value != null) {
                    binding.receiptImageBig.setImageBitmap(receiptImageViewModel.bitmapCropped.value)
                }
            }
        }
        receiptDataViewModel.product.observe(viewLifecycleOwner) {
            it?.let {
                productListAdapter.productList = it
                productListAdapter.notifyDataSetChanged()
            }
        }

    }

    @ExperimentalGetImage
    private fun analyzeImage() {
        val imageAnalyzer = ImageAnalyzer()
        receiptImageViewModel.bitmapCropped.value?.let {
            imageAnalyzer.analyzeProductList(
                InputImage.fromBitmap(it, 0)
            ) {
                receiptDataViewModel.product.value = imageAnalyzer.productList
                val analyzed =
                    imageAnalyzer.receiptLines.map { ExperimentalAdapterArgument(it) } as ArrayList<ExperimentalAdapterArgument>
                receiptDataViewModel.experimental.value = analyzed
                receiptDataViewModel.experimentalOriginal.value =
                    analyzed.toMutableList() as ArrayList<ExperimentalAdapterArgument>
            }
        }
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (receiptImageViewModel.uriCropped.value == null) {
            startCameraWithUri()
        }

        recyclerViewEvent = binding.recyclerViewEvent
        productListAdapter = ProductListAdapter(
            requireContext(),
            receiptDataViewModel.product.value ?: arrayListOf(), this
        ) { i: Int ->
            receiptDataViewModel.product.value?.get(i)?.let {
                DeleteConfirmationDialog(
                    "Are you sure you want to delete the product??\n\n"
                            + "Name: " + it.name + "\nPLN: " + it.finalPrice
                ) {
                    if (it.id >= 0) {
                        receiptDataViewModel.deleteProduct(it.id)
                    }
                    receiptDataViewModel.product.value?.removeAt(i)
                    productListAdapter.productList =
                        receiptDataViewModel.product.value ?: arrayListOf()
                    productListAdapter.notifyDataSetChanged()
                }.show(childFragmentManager, "TAG")
            }
        }

        initObserver()
        binding.receiptImageBig.setOnLongClickListener {
            startCameraWithUri()
            true
        }
        binding.storeDetails.text = receiptDataViewModel.savedStore.value?.name
        recyclerViewEvent.adapter = productListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        binding.reorderButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_shopListFragment_to_experimentRecycleFragment)
        }

        binding.addNewButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_shopListFragment_to_addProductFragment)
        }
        binding.confirmButton.setOnClickListener {
            receiptDataViewModel.convertDTOToProduct()
            receiptDataViewModel.insertProducts()
            Navigation.findNavController(binding.root).popBackStack(R.id.menuFragment, false)
        }
    }


    private val customCropImage = registerForActivityResult(CropImageContract()) {
        if (it !is CropImage.CancelledResult) {
            handleCropImageResult(it.uriContent)
        }
    }

    @ExperimentalGetImage
    private fun handleCropImageResult(uri: Uri?) {
        val bitmap = BitmapFactory.decodeStream(uri?.let {
            requireContext().contentResolver.openInputStream(it)
        })

        receiptImageViewModel.bitmapCropped.value = bitmap
        receiptImageViewModel.setImageUriCropped()
        analyzeImage()
    }

    private fun startCameraWithUri() {
        customCropImage.launch(
            CropImageContractOptions(
                uri = receiptImageViewModel.uri.value,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeCamera = false,
                    imageSourceIncludeGallery = false,
                ),
            ),
        )
    }

    override fun onItemClick(productIndex: Int) {

        val action =
            AddProductsFragmentDirections.actionShopListFragmentToAddProductFragment(productIndex)
        Navigation.findNavController(requireView()).navigate(action)

    }

}
