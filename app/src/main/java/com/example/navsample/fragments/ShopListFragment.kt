package com.example.navsample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.CustomAdapter
import com.example.navsample.DTO.Product
import com.example.navsample.DatabaseHelper
import com.example.navsample.ImageAnalyzer
import com.example.navsample.R
import com.example.navsample.databinding.FragmentShopListBinding
import com.google.mlkit.vision.common.InputImage
import java.util.ArrayList


class ShopListFragment : Fragment(), CustomAdapter.ItemClickListener {

    private var _binding: FragmentShopListBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    val args: ShopListFragmentArgs by navArgs()

    private lateinit var databaseHelper: DatabaseHelper
    private var productList: ArrayList<Product> = ArrayList()
    private var receiptId: String? = "receiptId"
    private var analyzedImage: InputImage? = null
    private val imageAnalyzer = ImageAnalyzer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopListBinding.inflate(inflater, container, false)

        return binding.root
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseHelper = DatabaseHelper(requireContext())
        recyclerViewEvent = binding.recyclerViewEvent
        customAdapter = CustomAdapter(requireActivity(), requireContext(), productList, this)
        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        storeDataInArrays()
        customAdapter.notifyDataSetChanged()
        /////
        binding.receiptImageBig.visibility = View.INVISIBLE

        binding.receiptImage.setOnClickListener {
            binding.receiptImageBig.visibility = View.VISIBLE
            binding.receiptImage.visibility = View.INVISIBLE
            binding.cameraButton.visibility = View.INVISIBLE
            binding.storageButton.visibility = View.INVISIBLE
            binding.manualButton.visibility = View.INVISIBLE


        }
        binding.receiptImageBig.setOnClickListener {
            binding.receiptImageBig.visibility = View.INVISIBLE
            binding.receiptImage.visibility = View.VISIBLE
            binding.cameraButton.visibility = View.VISIBLE
            binding.storageButton.visibility = View.VISIBLE
            binding.manualButton.visibility = View.VISIBLE
        }


        binding.cameraButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_shopListFragment_to_cameraFragment)
        }
        binding.manualButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_shopListFragment_to_addProductFragment)
        }
        binding.receiptInfo.setOnClickListener{
            binding.receiptInfo.setText(
                "valueNIP " + imageAnalyzer.valueNIP +
                        "\nvalidNIP?" + imageAnalyzer.validNIP.toString() +
                        "\ncompanyName " + imageAnalyzer.companyName +
                        "\nvaluePTU " + imageAnalyzer.valuePTU +
                        "\nvaluePLN " + imageAnalyzer.valuePLN +
                        "\nvalueDate " + imageAnalyzer.valueDate +
                        "\nvalueTime " + imageAnalyzer.valueTime

            )
        }

        val pickPhoto = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                analyzedImage = InputImage.fromFilePath(requireContext(), it)
                binding.receiptImage.setImageURI(it)
                binding.receiptImageBig.setImageURI(it)



                val text = analyzedImage?.let { it1 -> imageAnalyzer.processImageProxy(it1) }

            }

        }
        binding.storageButton.setOnClickListener {
            pickPhoto.launch("image/*")

        }

        if (args.bitmap != null) {
            binding.receiptImage.setImageBitmap(args.bitmap)
        }

    }

    private fun storeDataInArrays() {
        productList.clear()
        productList.addAll(databaseHelper.readAllProductData())
        if (productList.size == 0) {
            Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onItemClick(product: Product) {

        val action = ShopListFragmentDirections.actionShopListFragmentToAddProductFragment(product)
        Navigation.findNavController(requireView()).navigate(action)

    }
}