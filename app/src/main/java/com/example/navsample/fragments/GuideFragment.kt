package com.example.navsample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.navsample.R
import com.example.navsample.databinding.FragmentGuideMenuBinding
import com.example.navsample.imageanalyzer.GeminiAssistant
import kotlinx.coroutines.launch


class GuideFragment : Fragment() {
    private var _binding: FragmentGuideMenuBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val geminiAssistant = GeminiAssistant()
        viewLifecycleOwner.lifecycleScope.launch {
            val response = geminiAssistant.sendRequest(
                "\"D_JZN_ZIEMNIAKI JAD\" \n" +
                        "\"D_WAFLE_SONKO 130G\""
            )
            Log.i("Gemini", response ?: "EMPTY")
        }



        binding.test.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_guideFragment_to_imageImportGuideFragment)
        }

    }
}
