@file:Suppress("KotlinConstantConditions")

package com.example.navsample.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.R
import com.example.navsample.adapters.sorting.AlgorithmItemListAdapter
import com.example.navsample.adapters.sorting.SortingItemListAdapter
import com.example.navsample.adapters.sorting.UserItemListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.dto.SortingElementAction
import com.example.navsample.dto.SortingElementMode
import com.example.navsample.dto.Type
import com.example.navsample.dto.analyzer.AnalyzedProductsData
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.dto.sorting.ItemAdapterArgument
import com.example.navsample.dto.sorting.UserItemAdapterArgument
import com.example.navsample.imageanalyzer.ReceiptParser
import com.example.navsample.sorting.NonScrollableGridLayoutManager
import com.example.navsample.sorting.operation.ElementOperationHelper
import com.example.navsample.viewmodels.ExperimentalDataViewModel
import com.example.navsample.viewmodels.ImageAnalyzerViewModel
import com.example.navsample.viewmodels.ImageViewModel


open class ExperimentRecycleFragment : Fragment() {
    private var _binding: FragmentExperimentRecycleBinding? = null

    private val binding get() = _binding!!

    private val args: ExperimentRecycleFragmentArgs by navArgs()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val experimentalDataViewModel: ExperimentalDataViewModel by activityViewModels()
    private val imageAnalyzerViewModel: ImageAnalyzerViewModel by activityViewModels()
    private lateinit var algorithmOrderedPricesAdapter: AlgorithmItemListAdapter
    private lateinit var algorithmOrderedNamesAdapter: AlgorithmItemListAdapter
    private lateinit var userOrderedPricesAdapter: UserItemListAdapter
    private lateinit var userOrderedNamesAdapter: UserItemListAdapter
    private var recycleListAlgorithmPrices = arrayListOf<AlgorithmItemAdapterArgument>()
    private var recycleListAlgorithmNames = arrayListOf<AlgorithmItemAdapterArgument>()
    private var recycleListUserPrices = arrayListOf<UserItemAdapterArgument>()
    private var recycleListUserNames = arrayListOf<UserItemAdapterArgument>()

    private lateinit var receiptParser: ReceiptParser
    private lateinit var elementOperationHelper: ElementOperationHelper

    private var sortingMode = SortingElementMode.SWITCHING
    private var editingMode = false
    private var currentType = Type.NAME

    private var activePriceColor = 0
    private var activeNameColor = 0
    private var inactiveColor = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExperimentRecycleBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun configureReceiptParser() {
        args.receiptId.ifEmpty {
            Toast.makeText(requireContext(), "NO RECEIPT ID", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(requireView()).popBackStack()
            return
        }
        args.categoryId.ifEmpty {
            Toast.makeText(requireContext(), "NO CATEGORY ID", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(requireView()).popBackStack()
            return
        }
        receiptParser = ReceiptParser(
            args.receiptId,
            args.categoryId
        )


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activePriceColor = resources.getColor(R.color.blue_200, requireContext().theme)
        activeNameColor = resources.getColor(R.color.green_200, requireContext().theme)
        inactiveColor = resources.getColor(R.color.background_color, requireContext().theme)

        super.onViewCreated(view, savedInstanceState)
        initObserver()
        binding.editGrid.visibility = View.GONE

        configureReceiptParser()
        binding.switchingButton.isChecked = true
        readList()


        binding.switchingButton.setOnClickListener {
            if (sortingMode == SortingElementMode.SWITCHING) {
                changeCurrentType()
                changeCurrentType(currentType, true)
            }
            sortingMode = SortingElementMode.SWITCHING
        }
        binding.nameOnlyButton.setOnClickListener {
            sortingMode = SortingElementMode.NAME_ONLY
            changeCurrentType(Type.NAME, currentType != Type.NAME)
        }
        binding.priceOnlyButton.setOnClickListener {
            sortingMode = SortingElementMode.PRICE_ONLY
            changeCurrentType(Type.PRICE, currentType != Type.PRICE)
        }

        configureListAdapter()
        setupRecycleViews()

        binding.resetButton.setOnClickListener {
            algorithmOrderedPricesAdapter.setNewData(
                createDeepCopyOfAlgorithmElements(experimentalDataViewModel.algorithmOrderedPrices.value)
            )
            algorithmOrderedNamesAdapter.setNewData(
                createDeepCopyOfAlgorithmElements(experimentalDataViewModel.algorithmOrderedNames.value)
            )
            userOrderedNamesAdapter.setNewData(arrayListOf())
            userOrderedPricesAdapter.setNewData(arrayListOf())

            uncheckAll()
            setDefaultStatusAll()
        }

        binding.cancelButton.setOnClickListener {
            binding.editButton.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.GONE
            binding.editGrid.visibility = View.GONE
            binding.relativeLayout1.visibility = View.VISIBLE
            editingMode = false
            uncheckAll()
        }
        binding.editButton.setOnClickListener {
            binding.editButton.visibility = View.GONE
            binding.cancelButton.visibility = View.VISIBLE
            binding.editGrid.visibility = View.VISIBLE
            binding.relativeLayout1.visibility = View.GONE
            editingMode = true
        }

        binding.deleteButton.setOnClickListener {
            elementOperationHelper.executeElementOperation(SortingElementAction.DELETE)
        }
        binding.clearButton.setOnClickListener {
            elementOperationHelper.executeElementOperation(SortingElementAction.CLEAR_VALUE)
        }
        binding.mergeButton.setOnClickListener {
            elementOperationHelper.executeElementOperation(SortingElementAction.MERGE)
        }
        binding.swapSelectedButton.setOnClickListener {
            elementOperationHelper.executeElementOperation(SortingElementAction.SWAP)
        }

        binding.confirmButton.setOnClickListener {
            if (args.developerMode) {
                Navigation.findNavController(binding.root).popBackStack()
                return@setOnClickListener
            }

            val names = elementOperationHelper.userNames.map { it.value }
            val prices = elementOperationHelper.userPrices.map { it.value }
            experimentalDataViewModel.algorithmOrderedNames.value =
                createAlgorithmElementsFromUserElements(recycleListUserNames)
            experimentalDataViewModel.algorithmOrderedPrices.value =
                createAlgorithmElementsFromUserElements(recycleListUserPrices)

            val productsData =
                AnalyzedProductsData(
                    ArrayList(names),
                    ArrayList(prices),
                    receiptParser.parseToProducts(names, prices)
                )
            imageAnalyzerViewModel.aiAnalyze(
                productsData,
                experimentalDataViewModel.categoryList.value
            )

            Navigation.findNavController(binding.root).popBackStack()
        }
    }

    private fun initObserver() {
        imageViewModel.bitmapCroppedProduct.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.receiptImage.visibility = View.VISIBLE
                binding.receiptImage.setImageBitmap(imageViewModel.bitmapCroppedProduct.value)
            } else {
                binding.receiptImage.visibility = View.GONE
            }
        }
    }


    private fun changeCurrentType(type: Type, animate: Boolean) {
        val activateNameAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), inactiveColor, activeNameColor)
        val deactivateNameAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), activeNameColor, inactiveColor)
        val activatePriceAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), inactiveColor, activePriceColor)
        val deactivatePriceAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), activePriceColor, inactiveColor)

        activateNameAnimation.duration = 250
        deactivateNameAnimation.duration = 250
        activatePriceAnimation.duration = 250
        deactivatePriceAnimation.duration = 250

        activateNameAnimation.addUpdateListener { animator ->
            binding.cardViewUserName.setCardBackgroundColor(
                animator.animatedValue as Int
            )
        }
        deactivateNameAnimation.addUpdateListener { animator ->
            binding.cardViewUserName.setCardBackgroundColor(
                animator.animatedValue as Int
            )
        }
        activatePriceAnimation.addUpdateListener { animator ->
            binding.cardViewUserPrice.setCardBackgroundColor(
                animator.animatedValue as Int
            )
        }
        deactivatePriceAnimation.addUpdateListener { animator ->
            binding.cardViewUserPrice.setCardBackgroundColor(
                animator.animatedValue as Int
            )
        }

        when (type) {
            Type.PRICE -> {
                currentType = Type.PRICE
                if (animate) {
                    activatePriceAnimation.start()
                    deactivateNameAnimation.start()
                }
            }

            Type.NAME -> {
                currentType = Type.NAME
                binding.cardViewUserPrice.setCardBackgroundColor(
                    resources.getColor(
                        R.color.basic_grey,
                        requireContext().theme
                    )
                )
                binding.cardViewUserName.setCardBackgroundColor(
                    resources.getColor(
                        R.color.green_200,
                        requireContext().theme
                    )
                )
                if (animate) {
                    activateNameAnimation.start()
                    deactivatePriceAnimation.start()
                }
            }

            else -> {}
        }
    }

    private fun changeCurrentType() {
        when (currentType) {
            Type.PRICE -> {
                currentType = Type.NAME
            }

            Type.NAME -> {
                currentType = Type.PRICE
            }

            else -> {}
        }
    }

    private fun configureListAdapter() {
        //ALGORITHM     PRICE
        algorithmOrderedPricesAdapter = AlgorithmItemListAdapter(recycleListAlgorithmPrices,
            { position ->
                configureAlgorithmOneClick(recycleListAlgorithmPrices[position])
                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            }, { position ->
                editElement(position, recycleListAlgorithmPrices, algorithmOrderedPricesAdapter)
            }, requireContext()
        )

        //ALGORITHM     NAME
        algorithmOrderedNamesAdapter = AlgorithmItemListAdapter(recycleListAlgorithmNames,
            { position ->
                configureAlgorithmOneClick(recycleListAlgorithmNames[position])
                algorithmOrderedNamesAdapter.notifyItemChanged(position)
            }, { position ->
                editElement(position, recycleListAlgorithmNames, algorithmOrderedNamesAdapter)
            }, requireContext()
        )

        //USER     NAME
        userOrderedNamesAdapter = UserItemListAdapter(recycleListUserNames,
            { position ->
                elementOperationHelper.clickUserElement(
                    recycleListUserNames, position, userOrderedNamesAdapter
                )
            }, { position ->
                editElement(position, recycleListUserNames, userOrderedNamesAdapter)
            })

        //USER     PRICE
        userOrderedPricesAdapter = UserItemListAdapter(recycleListUserPrices,
            { position ->
                elementOperationHelper.clickUserElement(
                    recycleListUserPrices, position, userOrderedPricesAdapter
                )
            }, { position ->
                editElement(position, recycleListUserPrices, userOrderedPricesAdapter)
            })

        elementOperationHelper = ElementOperationHelper(
            recycleListAlgorithmPrices,
            recycleListAlgorithmNames,
            algorithmOrderedNamesAdapter,
            algorithmOrderedPricesAdapter,
            recycleListUserPrices,
            recycleListUserNames,
            userOrderedNamesAdapter,
            userOrderedPricesAdapter,
        )
    }

    private fun editElement(
        position: Int,
        item: List<ItemAdapterArgument>,
        adapter: SortingItemListAdapter<*>
    ) {
        elementOperationHelper.editSingleElement(position, item[position], adapter) {
            it.show(childFragmentManager, "TAG")
        }
    }

    private fun configureAlgorithmOneClick(item: AlgorithmItemAdapterArgument) {
        if (item.type != Type.UNDEFINED) {
            return
        }
        if (editingMode) {
            elementOperationHelper.checkAndUncheckSingleElement(item)
        } else {
            elementOperationHelper.clickAlgorithmElement(item, currentType)
            if (sortingMode == SortingElementMode.SWITCHING) {
                changeCurrentType()
                changeCurrentType(currentType, true)
            }
        }
    }

    private fun readList() {
        recycleListAlgorithmNames =
            createDeepCopyOfAlgorithmElements(experimentalDataViewModel.algorithmOrderedNames.value)
        recycleListAlgorithmPrices =
            createDeepCopyOfAlgorithmElements(experimentalDataViewModel.algorithmOrderedPrices.value)
        recycleListUserNames =
            experimentalDataViewModel.userOrderedName.value?.let { ArrayList(it) } ?: arrayListOf()
        recycleListUserPrices =
            experimentalDataViewModel.userOrderedPrices.value?.let { ArrayList(it) }
                ?: arrayListOf()
    }

    private fun setupRecycleViews() {
        binding.recyclerViewUserPrice.verticalScrollbarPosition = View.SCROLLBAR_POSITION_LEFT
        binding.recyclerViewAlgorithmPrice.verticalScrollbarPosition = View.SCROLLBAR_POSITION_LEFT

        binding.recyclerViewUserName.adapter = userOrderedNamesAdapter
        binding.recyclerViewUserName.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewUserPrice.adapter = userOrderedPricesAdapter
        binding.recyclerViewUserPrice.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewAlgorithmName.adapter = algorithmOrderedNamesAdapter
        binding.recyclerViewAlgorithmName.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewAlgorithmPrice.adapter = algorithmOrderedPricesAdapter
        binding.recyclerViewAlgorithmPrice.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
    }

    private fun createDeepCopyOfAlgorithmElements(list: List<AlgorithmItemAdapterArgument>?): ArrayList<AlgorithmItemAdapterArgument> {
        if (list == null) {
            return arrayListOf()
        }
        val newList = ArrayList<AlgorithmItemAdapterArgument>()
        list.forEach {
            newList.add(AlgorithmItemAdapterArgument(it))
        }
        return newList
    }

    private fun createAlgorithmElementsFromUserElements(list: List<UserItemAdapterArgument>?): ArrayList<AlgorithmItemAdapterArgument> {
        if (list == null) {
            return arrayListOf()
        }
        val newList = ArrayList<AlgorithmItemAdapterArgument>()
        list.forEach {
            newList.add(AlgorithmItemAdapterArgument(it.value))
        }
        return newList
    }

    private fun uncheckAll() {
        elementOperationHelper.executeElementOperation(SortingElementAction.UNCHECK)
    }

    private fun setDefaultStatusAll() {
        elementOperationHelper.executeElementOperation(SortingElementAction.CLEAR_STATUS)
    }

}
