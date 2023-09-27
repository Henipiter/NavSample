package com.example.navsample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.navsample.databinding.FragmentAddProductBinding

class AddProductFragment : Fragment() {

        private var _binding: FragmentAddProductBinding? = null
        private val binding get() = _binding!!





        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentAddProductBinding.inflate(inflater, container, false)

            return binding.root
        }
}