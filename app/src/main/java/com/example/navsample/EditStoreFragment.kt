package com.example.navsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.navsample.databinding.EditTextDialogBinding
import com.example.navsample.databinding.FragmentEditStoreBinding
import com.example.navsample.databinding.FragmentStoreListBinding
import com.example.navsample.fragments.AddSingleProductFragmentArgs
import com.example.navsample.viewmodels.ReceiptDataViewModel

class EditStoreFragment : Fragment() {
    private var _binding: FragmentEditStoreBinding? = null
    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private val args: EditStoreFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.storeIndex != -1) {
            val store = receiptDataViewModel.storeList.value?.get(args.storeIndex)

            binding.storeNameInput.setText(store?.name)
            binding.nipInput.setText(store?.nip)
        }
    }
}
