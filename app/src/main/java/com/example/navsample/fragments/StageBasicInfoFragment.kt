package com.example.navsample.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.databinding.FragmentStageBasicInfoBinding


class StageBasicInfoFragment : Fragment() {
    private var _binding: FragmentStageBasicInfoBinding? = null
    private val binding get() = _binding!!
    val args: StageBasicInfoFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStageBasicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.uri != null) {
            binding.receiptImageMarked.setImageURI(args.uri)
            binding.storeNameInput.setText(args.receipt?.storeName)
            binding.storeNIPInput.setText(args.receipt?.storeNIP)
            binding.receiptPTUInput.setText(args.receipt?.receiptPTU)
            binding.receiptPLNInput.setText(args.receipt?.receiptPLN)
            binding.receiptDateInput.setText(args.receipt?.receiptDate)
            binding.receiptTimeInput.setText(args.receipt?.receiptTime)
        }
        binding.addProductsButton.setOnClickListener{
            val action = StageBasicInfoFragmentDirections.actionStageBasicInfoFragmentToShopListFragment()
            Navigation.findNavController(it).navigate(action)
        }

    }


}