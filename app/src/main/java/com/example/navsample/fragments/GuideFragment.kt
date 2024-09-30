package com.example.navsample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentGuideMenuBinding
import com.example.navsample.viewmodels.GeminiViewModel


class GuideFragment : Fragment() {
    private var _binding: FragmentGuideMenuBinding? = null

    private val binding get() = _binding!!

    private val geminiViewModel: GeminiViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        geminiViewModel.sendRequest(
            "\"D_JZN_ZIEMNIAKI JAD\" \n" +
                    "\"D_WAFLE_SONKO 130G\""
        )


        binding.test.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_guideFragment_to_imageImportGuideFragment)
        }

    }

    private fun initObserver() {
        geminiViewModel.response.observe(viewLifecycleOwner) {
            Log.i("Gemini", it ?: "EMPTY")
        }
    }
}
