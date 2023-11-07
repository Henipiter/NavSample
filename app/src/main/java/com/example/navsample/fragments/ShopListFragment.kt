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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.navsample.CustomAdapter
import com.example.navsample.ImageAnalyzer
import com.example.navsample.R
import com.example.navsample.databinding.FragmentShopListBinding
import com.example.navsample.entities.Product
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch


@ExperimentalGetImage
class ShopListFragment : Fragment(), CustomAdapter.ItemClickListener {

    private var _binding: FragmentShopListBinding? = null
    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter

//    private var productList: ArrayList<Product> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopListBinding.inflate(inflater, container, false)
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
                customAdapter.productList = it
                customAdapter.notifyDataSetChanged()
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
            }
        }
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        startCameraWithUri()

        recyclerViewEvent = binding.recyclerViewEvent
        customAdapter = CustomAdapter(
            requireContext(),
            receiptDataViewModel.product.value ?: arrayListOf(), this
        ) { i: Int ->
            receiptDataViewModel.product.value?.removeAt(i)
            customAdapter.productList = receiptDataViewModel.product.value ?: arrayListOf()
            customAdapter.notifyDataSetChanged()
        }


        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        binding.addNewButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_shopListFragment_to_addProductFragment)
        }
        binding.confirmButton.setOnClickListener {
            convertProducts()
            lifecycleScope.launch {
                val dao = ReceiptDatabase.getInstance(requireContext()).receiptDao
                receiptDataViewModel.savedProduct.value?.forEach { product ->
                    dao.insertProduct(product)
                }
            }

            Navigation.findNavController(binding.root).popBackStack(R.id.menuFragment, false)
        }
    }

    private fun transformToFloat(value: String): Float {
        return try {
            value.replace(",", ".").toFloat()
        } catch (t: Throwable) {
            0.0f
        }
    }

    private fun convertProducts() {
        val newProducts = ArrayList<Product>()
        receiptDataViewModel.product.value?.forEach { productDTO ->
            newProducts.add(
                Product(
                    receiptDataViewModel.savedReceipt.value?.id
                        ?: throw IllegalArgumentException("No ID of receipt"),
                    productDTO.name.toString(),
                    productDTO.category.toString(),
                    transformToFloat(productDTO.amount.toString()),
                    transformToFloat(productDTO.itemPrice.toString()),
                    transformToFloat(productDTO.finalPrice.toString()),
                    productDTO.ptuType.toString(),
                )
            )
        }
        receiptDataViewModel.savedProduct.value = newProducts


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
            ShopListFragmentDirections.actionShopListFragmentToAddProductFragment(productIndex)
        Navigation.findNavController(requireView()).navigate(action)

    }

}
