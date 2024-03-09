package com.example.navsample.fragments.saving


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
import com.example.navsample.ImageAnalyzer
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.ProductListAdapter
import com.example.navsample.databinding.FragmentAddProductListBinding
import com.example.navsample.dto.ExperimentalAdapterArgument
import com.example.navsample.exception.NoReceiptIdException
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.mlkit.vision.common.InputImage


@ExperimentalGetImage
class AddProductListFragment : Fragment(), ItemClickListener {

    private var _binding: FragmentAddProductListBinding? = null
    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProductListBinding.inflate(inflater, container, false)
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

        receiptDataViewModel.reorderedProductTiles.observe(viewLifecycleOwner) {
            if (it == true) {
                receiptDataViewModel.reorderedProductTiles.value = false
                reorderTilesWithProducts()
            }
        }

    }

    @ExperimentalGetImage
    private fun analyzeImage() {
        val receiptId = receiptDataViewModel.receipt.value?.id ?: throw NoReceiptIdException()
        val imageAnalyzer = ImageAnalyzer()
        receiptImageViewModel.bitmapCropped.value?.let {
            imageAnalyzer.analyzeProductList(
                InputImage.fromBitmap(it, 0),
                receiptId
            ) {
                receiptDataViewModel.product.value = imageAnalyzer.productList
                val analyzed =
                    imageAnalyzer.receiptLines.map { ExperimentalAdapterArgument(it) } as ArrayList<ExperimentalAdapterArgument>
                receiptDataViewModel.experimental.value = analyzed
                receiptDataViewModel.experimentalOriginal.value =
                    analyzed.toMutableList() as ArrayList<ExperimentalAdapterArgument>
                receiptDataViewModel.reorderedProductTiles.value = true
            }
        }
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (receiptImageViewModel.uriCropped.value == null) {
            startCameraWithUri()
        }
        initObserver()
        recyclerViewEvent = binding.recyclerViewEvent
        productListAdapter = ProductListAdapter(
            requireContext(),
            receiptDataViewModel.product.value ?: arrayListOf(),
            receiptDataViewModel.categoryList.value ?: arrayListOf(),
            this
        ) { i: Int ->
            receiptDataViewModel.product.value?.get(i)?.let {
                DeleteConfirmationDialog(
                    "Are you sure you want to delete the product??\n\n"
                            + "Name: " + it.name + "\nPLN: " + it.finalPrice
                ) {
                    if (it.id != null && it.id!! >= 0) {
                        receiptDataViewModel.deleteProduct(it.id!!)
                    }
                    receiptDataViewModel.product.value?.removeAt(i)
                    productListAdapter.productList =
                        receiptDataViewModel.product.value ?: arrayListOf()
                    productListAdapter.notifyDataSetChanged()
                }.show(childFragmentManager, "TAG")
            }
        }


        binding.receiptImageBig.setOnLongClickListener {
            startCameraWithUri()
            true
        }
        binding.storeDetails.text = receiptDataViewModel.store.value?.name
        recyclerViewEvent.adapter = productListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        binding.reorderButton.setOnClickListener {
            reorderTilesWithProducts()
        }

        binding.addNewButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_addProductListFragment_to_addProductFragment)
        }
        binding.confirmButton.setOnClickListener {
            receiptDataViewModel.insertProducts(
                receiptDataViewModel.product.value?.toList() ?: listOf()
            )
            Navigation.findNavController(binding.root).popBackStack(R.id.menuFragment, false)
        }
    }


    private val customCropImage = registerForActivityResult(CropImageContract()) {
        if (it !is CropImage.CancelledResult) {
            handleCropImageResult(it.uriContent)
        }
    }

    private fun reorderTilesWithProducts() {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_addProductListFragment_to_experimentRecycleFragment)

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

    override fun onItemClick(index: Int) {
        val action =
            AddProductListFragmentDirections.actionAddProductListFragmentToAddProductFragment(
                false, index
            )
        Navigation.findNavController(requireView()).navigate(action)

    }

}
