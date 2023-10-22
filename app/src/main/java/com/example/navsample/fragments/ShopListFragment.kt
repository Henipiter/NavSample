package com.example.navsample.fragments


import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.navsample.CustomAdapter
import com.example.navsample.DTO.Product
import com.example.navsample.DatabaseHelper
import com.example.navsample.R
import com.example.navsample.databinding.FragmentShopListBinding


class ShopListFragment : Fragment(), CustomAdapter.ItemClickListener {

    private var _binding: FragmentShopListBinding? = null
    private val binding get() = _binding!!
    private val args: ShopListFragmentArgs by navArgs()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter

    private lateinit var databaseHelper: DatabaseHelper
    private var productList: ArrayList<Product> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(args.uri != null) {
            binding.receiptImageBig.setImageURI(args.uri)
        }
        super.onViewCreated(view, savedInstanceState)
        databaseHelper = DatabaseHelper(requireContext())
        recyclerViewEvent = binding.recyclerViewEvent
        customAdapter = CustomAdapter(requireActivity(), requireContext(), productList, this)
        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

//        storeDataInArrays()
        storeDataInArraysFromFragment()
        customAdapter.notifyDataSetChanged()

        binding.addNewButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_shopListFragment_to_addProductFragment)
        }
        binding.confirmButton.setOnClickListener {


            Navigation.findNavController(binding.root).popBackStack(R.id.menuFragment,false)
        }
        binding.receiptImageBig.setOnLongClickListener{
            startCameraWithUri()

            Toast.makeText(requireContext(), "AA", Toast.LENGTH_SHORT).show();
            true
        }
    }

    private val customCropImage = registerForActivityResult(CropImageContract()) {
        if (it !is CropImage.CancelledResult) {
            handleCropImageResult(it.uriContent)
        }
    }
    private fun handleCropImageResult(uri: Uri?) {
        val bitmap = BitmapFactory.decodeStream(uri?.let {
            requireContext().contentResolver.openInputStream(it)
        })
//        binding.receiptImageBig.setImageBitmap(bitmap)
    }
    private fun startCameraWithUri() {
        customCropImage.launch(
            CropImageContractOptions(
                uri = args.uri,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeCamera = false,
                    imageSourceIncludeGallery = false,
                ),
            ),
        )
    }

    private fun storeDataInArrays() {
        productList.clear()
        productList.addAll(databaseHelper.readAllProductData())
        if (productList.size == 0) {
            Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show()
        }
    }
    private fun storeDataInArraysFromFragment(){
        productList.clear()
        if(args.productList!=null) {
            productList.addAll(args.productList!!)
        }
        else{
            productList.clear()
        }
    }

    override fun onItemClick(product: Product) {

        val action = ShopListFragmentDirections.actionShopListFragmentToAddProductFragment(product)
        Navigation.findNavController(requireView()).navigate(action)

    }

}