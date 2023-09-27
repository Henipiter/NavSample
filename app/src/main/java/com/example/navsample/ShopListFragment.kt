package com.example.navsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.databinding.FragmentMenuBinding
import com.example.navsample.databinding.FragmentShopListBinding


class ShopListFragment : Fragment() {

    private var _binding: FragmentShopListBinding? = null

    private val binding get() = _binding!!

    val args: ShopListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopListBinding.inflate(inflater, container, false)

        binding.receiptImage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_shopListFragment_to_cameraFragment)
        }

        val bitmap = args.bitmap
        binding.receiptImage.setImageBitmap(bitmap)

        return binding.root
    }


}