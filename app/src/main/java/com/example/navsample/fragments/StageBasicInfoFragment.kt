package com.example.navsample.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

            if (!verifyNIP(args.receipt?.storeNIP)){
                binding.storeNIPInputInfo.visibility = View.VISIBLE
            }

        }
        binding.addProductsButton.setOnClickListener{
            val action = StageBasicInfoFragmentDirections.actionStageBasicInfoFragmentToShopListFragment()
            Navigation.findNavController(it).navigate(action)
        }
        binding.storeNIPInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!verifyNIP(args.receipt?.storeNIP)){
                    binding.storeNIPInputInfo.visibility = View.VISIBLE
                }
                else{
                    binding.storeNIPInputInfo.visibility = View.INVISIBLE
                }

            }
        }

    }
    private fun verifyNIP(valueNIP: String?): Boolean {
        if (valueNIP == null || !Regex("""[0-9]{10}""").matches(valueNIP)) {
            return false
        }
        val weight = arrayOf(6, 5, 7, 2, 3, 4, 5, 6, 7)
        var sum = 0
        for (i in 0..8) {
            sum += valueNIP[i].digitToInt() * weight[i]
        }
        return sum % 11 == valueNIP[9].digitToInt()
    }

}