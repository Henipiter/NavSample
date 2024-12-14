package com.example.navsample.guide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.canhub.cropper.CropImageView
import com.example.navsample.R
import com.example.navsample.adapters.sorting.AlgorithmItemListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.guide.Guide
import com.example.navsample.sorting.NonScrollableGridLayoutManager
import com.github.chrisbanes.photoview.PhotoView


class ExperimentalListGuideFragment : Fragment(), Guide {
    private var _binding: FragmentExperimentRecycleBinding? = null
    private val binding get() = _binding!!

    override var iterator: Int = 1
    override lateinit var instructions: List<() -> Unit>
    override lateinit var texts: List<String>
    override lateinit var verticalLevel: List<Int>

    private lateinit var algorithmOrderedPricesAdapter: AlgorithmItemListAdapter
    private lateinit var algorithmOrderedNamesAdapter: AlgorithmItemListAdapter
    private var recycleListAlgorithmPrices = arrayListOf<AlgorithmItemAdapterArgument>()
    private var recycleListAlgorithmNames = arrayListOf<AlgorithmItemAdapterArgument>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExperimentRecycleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepare()
        configureDialog().show(childFragmentManager, "TAG")
    }

    private fun prepareLists() {
        recycleListAlgorithmNames = arrayListOf(
            AlgorithmItemAdapterArgument("B_IMPIR LUZ*"),
            AlgorithmItemAdapterArgument("D_KAJZERKA 50 G LUZ*")
        )
        recycleListAlgorithmPrices = arrayListOf(
            AlgorithmItemAdapterArgument("0,005szt.w*24,89= 1,24 B"),
            AlgorithmItemAdapterArgument("15szt*0,37= 5,55 D")
        )

        algorithmOrderedPricesAdapter =
            AlgorithmItemListAdapter(recycleListAlgorithmPrices, { }, { }, requireContext())
        algorithmOrderedNamesAdapter =
            AlgorithmItemListAdapter(recycleListAlgorithmNames, { }, { }, requireContext())

        binding.recyclerViewAlgorithmName.adapter = algorithmOrderedNamesAdapter
        binding.recyclerViewAlgorithmName.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewAlgorithmPrice.adapter = algorithmOrderedPricesAdapter
        binding.recyclerViewAlgorithmPrice.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)

    }

    override fun prepare() {
        prepareLists()

        loadImage("short_crop_receipt.png")
        instructions = listOf(
            { Navigation.findNavController(requireView()).popBackStack() },
            { loadImage("short_crop_receipt.png") },
            {
                Navigation.findNavController(binding.root)
                    .popBackStack(R.id.guideFragment, false)
            }
        )
        texts = listOf(
            "Text",
            ""
        )
        verticalLevel = listOf(
            100, 100, 100
        )
    }


    override fun loadImage(imageName: String) {
        loadImage(imageName, requireContext())
    }

    override fun loadCropImageView(imageName: String) {
        TODO("Not yet implemented")
    }

    override fun getPhotoView(): PhotoView {
        return binding.receiptImage
    }

    override fun getCropImageView(): CropImageView {
        TODO("Not yet implemented")
    }
}
