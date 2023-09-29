package com.example.navsample.fragments


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
import com.example.navsample.CustomAdapter
import com.example.navsample.DTO.Product
import com.example.navsample.DatabaseHelper
import com.example.navsample.R
import com.example.navsample.databinding.FragmentShopListBinding


class ShopListFragment : Fragment(), CustomAdapter.ItemClickListener {

    private var _binding: FragmentShopListBinding? = null
    private val binding get() = _binding!!
    val args: ShopListFragmentArgs by navArgs()

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
        super.onViewCreated(view, savedInstanceState)
        databaseHelper = DatabaseHelper(requireContext())
        recyclerViewEvent = binding.recyclerViewEvent
        customAdapter = CustomAdapter(requireActivity(), requireContext(), productList, this)
        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        storeDataInArrays()
        customAdapter.notifyDataSetChanged()

        binding.manualButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_shopListFragment_to_addProductFragment)
        }

        if (args.bitmap != null) {
            binding.receiptImageBig.setImageBitmap(args.bitmap)
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