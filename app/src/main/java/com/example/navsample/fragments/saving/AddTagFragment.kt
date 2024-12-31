package com.example.navsample.fragments.saving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.navsample.databinding.FragmentAddTagBinding
import com.example.navsample.viewmodels.ListingViewModel

class AddTagFragment : Fragment() {
    private var _binding: FragmentAddTagBinding? = null
    private val binding get() = _binding!!

    private val listingViewModel: ListingViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddTagBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}
