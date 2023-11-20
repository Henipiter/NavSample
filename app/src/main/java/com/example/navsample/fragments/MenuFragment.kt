package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentMenuBinding
import com.example.navsample.viewmodels.ReceiptDataViewModel


class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null

    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addReceipt.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_imageImportFragment)
        }
        binding.showReceipt.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_receiptListFragment)
        }
        binding.showStores.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_storeListFragment)
        }
        binding.recycleViewTest.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_experimentRecycleFragment)
        }
        receiptDataViewModel.clearData()
    }
}
