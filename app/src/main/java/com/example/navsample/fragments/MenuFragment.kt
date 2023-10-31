package com.example.navsample.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentMenuBinding
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
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

        val dao = ReceiptDatabase.getInstance(requireContext()).receiptDao
        val products = listOf(
            Product(0, 0, "Chleb", 0, 1F, 1F, 1F, "A"),
            Product(1, 1, "Chleb", 0, 1F, 1F, 1F, "A"),
            Product(2, 1, "Ser", 0, 1F, 1F, 1F, "A")

        )
        val receipts = listOf(
            Receipt(0, "1", 2F, 1F),
            Receipt(1, "2", 2F, 1F)
        )

        lifecycleScope.launch {
            receipts.forEach {
                dao.insertReceipt(it)
            }
            products.forEach {
                dao.insertProduct(it)
            }
        }
        return binding.root
    }
}