package com.example.navsample.fragments.saving


import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.example.navsample.ItemClickListener
import com.example.navsample.R
import com.example.navsample.adapters.ProductListAdapter
import com.example.navsample.databinding.FragmentAddProductListBinding
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.exception.NoReceiptIdException
import com.example.navsample.exception.NoStoreIdException
import com.example.navsample.fragments.dialogs.DeleteConfirmationDialog
import com.example.navsample.imageanalyzer.ImageAnalyzer
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import com.google.mlkit.vision.common.InputImage


@ExperimentalGetImage
class AddProductListFragment : Fragment(), ItemClickListener {

    private var _binding: FragmentAddProductListBinding? = null
    private val binding get() = _binding!!

    private lateinit var myPref: SharedPreferences
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
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(receiptImageViewModel.bitmapCropped.value)
                binding.toolbar.menu.findItem(R.id.reorder).isVisible = true
            } else {
                binding.receiptImage.visibility = View.GONE
                binding.toolbar.menu.findItem(R.id.reorder).isVisible = false
            }
        }
        receiptDataViewModel.product.observe(viewLifecycleOwner) {
            it?.let {
                productListAdapter.productList = it
                productListAdapter.notifyDataSetChanged()
                recalculateSumOfPrices()
            }
        }

        receiptDataViewModel.reorderedProductTiles.observe(viewLifecycleOwner) {
            if (myPref.getBoolean(OPEN_REORDER_FRAGMENT, false) && it == true) {
                receiptDataViewModel.reorderedProductTiles.value = false
                reorderTilesWithProducts()
            }
        }

    }

    @ExperimentalGetImage
    private fun analyzeImage() {
        val receiptId = receiptDataViewModel.receipt.value?.id ?: throw NoReceiptIdException()
        val categoryId =
            receiptDataViewModel.store.value?.defaultCategoryId ?: throw NoStoreIdException()
        val imageAnalyzer = ImageAnalyzer()
        imageAnalyzer.uid = receiptImageViewModel.uid.value ?: "temp"
        receiptImageViewModel.bitmapCropped.value?.let { bitmap ->
            imageAnalyzer.analyzeProductList(
                InputImage.fromBitmap(bitmap, 0),
                receiptId,
                categoryId
            ) {
                receiptDataViewModel.product.value = imageAnalyzer.productList

                receiptDataViewModel.algorithmOrderedNames.value =
                    imageAnalyzer.receiptNameLines.map { AlgorithmItemAdapterArgument(it) } as ArrayList<AlgorithmItemAdapterArgument>
                receiptDataViewModel.algorithmOrderedPrices.value =
                    imageAnalyzer.receiptPriceLines.map { AlgorithmItemAdapterArgument(it) } as ArrayList<AlgorithmItemAdapterArgument>
                receiptDataViewModel.reorderedProductTiles.value = true
            }
        }
    }

    private fun recalculateSumOfPrices() {
        var sum = 0.0
        productListAdapter.productList.forEach {
            sum += it.finalPrice
        }
        sum = "%.2f".format(sum).toDouble()
        binding.cartValueText.text = sum.toString()

        val final = receiptDataViewModel.receipt.value?.pln
        if (final != sum) {
            binding.cartValueText.setTextColor(Color.RED)
        } else {
            binding.cartValueText.setTextColor(
                resources.getColor(
                    R.color.basic_text_grey,
                    requireContext().theme
                )
            )
        }
        binding.countText.text = productListAdapter.productList.size.toString()
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_extended_add)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.menu.findItem(R.id.edit).isVisible = false

        myPref =
            requireContext().getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)

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
                            + "Name: " + it.name + "\nPLN: " + it.subtotalPrice
                ) {
                    if (it.id != null && it.id!! >= 0) {
                        receiptDataViewModel.deleteProduct(it.id!!)
                    }
                    receiptDataViewModel.product.value?.removeAt(i)
                    productListAdapter.productList =
                        receiptDataViewModel.product.value ?: arrayListOf()
                    productListAdapter.notifyDataSetChanged()
                    recalculateSumOfPrices()
                }.show(childFragmentManager, "TAG")
            }
        }


        binding.receiptImage.setOnLongClickListener {
            startCameraWithUri()
            true
        }
        binding.toolbar.title = receiptDataViewModel.store.value?.name
        binding.receiptValueText.text = receiptDataViewModel.receipt.value?.pln.toString()
        recyclerViewEvent.adapter = productListAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reorder -> {
                    reorderTilesWithProducts()
                    true
                }

                R.id.add_new -> {
                    val action =
                        AddProductListFragmentDirections.actionAddProductListFragmentToAddProductFragment(
                            false, -1
                        )
                    Navigation.findNavController(requireView()).navigate(action)
                    true
                }

                R.id.confirm -> {
                    receiptDataViewModel.insertProducts(
                        receiptDataViewModel.product.value?.toList() ?: listOf()
                    )
                    Navigation.findNavController(binding.root)
                        .popBackStack(R.id.menuFragment, false)

                    true
                }

                else -> false
            }
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

    companion object {

        private const val OPEN_REORDER_FRAGMENT = "openReorderFragment"
    }

}
