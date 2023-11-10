package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentMenuBinding
import com.example.navsample.entities.Product
import com.example.navsample.entities.ReceiptDatabase
import kotlinx.coroutines.launch


class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)

        binding.addReceipt.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_imageImportFragment)
        }
        binding.showReceipt.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_storeListFragment)

        }
        binding.recycleViewTest.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_experimentRecycleFragment)

        }

        val dao = ReceiptDatabase.getInstance(requireContext()).receiptDao
        val product = Product(2, "1", "Ser", 0f, 1F, 1F, "A")
        lifecycleScope.launch { dao.insertProduct(product) }
        return binding.root
    }

}
