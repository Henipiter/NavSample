package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.navsample.databinding.FragmentStoreListBinding

class StoreListFragment : Fragment() {
    private var _binding: FragmentStoreListBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.productCategoryInput.doOnTextChanged { actual, start, _, count ->
            if(actual.toString().contains(" ") || Regex(".*[a-z].*").matches(actual.toString())){
                val fixed = actual.toString().replace(" ", "_").uppercase()
                binding.productCategoryInput.setText(fixed)
                binding.productCategoryInput.setSelection(start+count)
            }
        }

    }
}