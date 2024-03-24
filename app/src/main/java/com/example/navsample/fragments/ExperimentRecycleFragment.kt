@file:Suppress("KotlinConstantConditions")

package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.adapters.sorting.AlgorithmItemListAdapter
import com.example.navsample.adapters.sorting.SortingItemListAdapter
import com.example.navsample.adapters.sorting.UserItemListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.dto.SortingElementAction
import com.example.navsample.dto.SortingElementMode
import com.example.navsample.dto.Type
import com.example.navsample.dto.sorting.AlgorithmItemAdapterArgument
import com.example.navsample.dto.sorting.ItemAdapterArgument
import com.example.navsample.dto.sorting.UserItemAdapterArgument
import com.example.navsample.imageanalyzer.ReceiptParser
import com.example.navsample.sorting.NonScrollableGridLayoutManager
import com.example.navsample.sorting.operation.ElementOperationHelper
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel

open class ExperimentRecycleFragment : Fragment() {
    private var _binding: FragmentExperimentRecycleBinding? = null

    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExperimentRecycleBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        binding.editGrid.visibility = View.GONE
        receiptParser = ReceiptParser(
            receiptDataViewModel.receipt.value?.id ?: -1,            //throw NoReceiptIdException(),
            receiptDataViewModel.store.value?.defaultCategoryId ?: -1 // throw NoStoreIdException()
        )
        binding.switchingButton.isChecked = true
        binding.priceModeColor.visibility = View.INVISIBLE
        binding.nameModeColor.visibility = View.VISIBLE
        readList()


        binding.switchingButton.setOnClickListener {
            if (sortingMode == SortingElementMode.SWITCHING) {
                changeCurrentType()
            }
            sortingMode = SortingElementMode.SWITCHING
        }
        binding.nameOnlyButton.setOnClickListener {
            sortingMode = SortingElementMode.NAME_ONLY
            changeCurrentType(Type.NAME)
        }
        binding.priceOnlyButton.setOnClickListener {
            sortingMode = SortingElementMode.PRICE_ONLY
            changeCurrentType(Type.PRICE)
        }

        configureListAdapter()
        setupRecycleViews()

        binding.resetButton.setOnClickListener {
            algorithmOrderedPricesAdapter.setNewData(
                createDeepCopyOfAlgorithmElements(receiptDataViewModel.algorithmOrderedPrices.value)
            )
            algorithmOrderedNamesAdapter.setNewData(
                createDeepCopyOfAlgorithmElements(receiptDataViewModel.algorithmOrderedNames.value)
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
            val namePricePairs = elementOperationHelper.convertUserElementsToLines()
            receiptDataViewModel.algorithmOrderedNames.value =
                createAlgorithmElementsFromUserElements(recycleListUserNames)
            receiptDataViewModel.algorithmOrderedPrices.value =
                createAlgorithmElementsFromUserElements(recycleListUserPrices)
            receiptDataViewModel.product.value = receiptParser.parseToProducts(namePricePairs)
            Navigation.findNavController(binding.root).popBackStack()
        }
    }

    private fun initObserver() {
        receiptImageViewModel.bitmapCropped.observe(viewLifecycleOwner) {
            it?.let {
                if (receiptImageViewModel.bitmapCropped.value != null) {
                    binding.receiptImageBig.setImageBitmap(receiptImageViewModel.bitmapCropped.value)
                }
            }
        }
    }

    private fun changeCurrentType(type: Type) {
        when (type) {
            Type.PRICE -> {
                currentType = Type.PRICE
                binding.priceModeColor.visibility = View.VISIBLE
                binding.nameModeColor.visibility = View.INVISIBLE
            }

            Type.NAME -> {
                currentType = Type.NAME
                binding.priceModeColor.visibility = View.INVISIBLE
                binding.nameModeColor.visibility = View.VISIBLE
            }

            else -> {}
        }
    }

    private fun changeCurrentType() {
        when (currentType) {
            Type.PRICE -> {
                currentType = Type.NAME
                binding.priceModeColor.visibility = View.INVISIBLE
                binding.nameModeColor.visibility = View.VISIBLE
            }

            Type.NAME -> {
                currentType = Type.PRICE
                binding.priceModeColor.visibility = View.VISIBLE
                binding.nameModeColor.visibility = View.INVISIBLE
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
            })

        //ALGORITHM     NAME
        algorithmOrderedNamesAdapter = AlgorithmItemListAdapter(recycleListAlgorithmNames,
            { position ->
                configureAlgorithmOneClick(recycleListAlgorithmNames[position])
                algorithmOrderedNamesAdapter.notifyItemChanged(position)
            }, { position ->
                editElement(position, recycleListAlgorithmNames, algorithmOrderedNamesAdapter)
            })

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
            }
        }
    }

    private fun readList() {
        recycleListAlgorithmNames =
            createDeepCopyOfAlgorithmElements(receiptDataViewModel.algorithmOrderedNames.value)
        recycleListAlgorithmPrices =
            createDeepCopyOfAlgorithmElements(receiptDataViewModel.algorithmOrderedPrices.value)
        recycleListUserNames =
            receiptDataViewModel.userOrderedName.value?.let { ArrayList(it) } ?: arrayListOf()
        recycleListUserPrices =
            receiptDataViewModel.userOrderedPrices.value?.let { ArrayList(it) } ?: arrayListOf()
    }

    private fun setupRecycleViews() {
        binding.recyclerViewUserName.adapter = userOrderedNamesAdapter
        binding.recyclerViewUserName.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewUserPrice.adapter = userOrderedPricesAdapter
        binding.recyclerViewUserPrice.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewAlgorithmName.adapter = algorithmOrderedPricesAdapter
        binding.recyclerViewAlgorithmName.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewAlgorithmPrice.adapter = algorithmOrderedNamesAdapter
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
