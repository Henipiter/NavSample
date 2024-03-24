@file:Suppress("KotlinConstantConditions")

package com.example.navsample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.adapters.UserItemListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.SortingElementAction
import com.example.navsample.dto.SortingElementMode
import com.example.navsample.dto.Status
import com.example.navsample.dto.Type
import com.example.navsample.dto.UserItemAdapterArgument
import com.example.navsample.fragments.dialogs.EditTextDialog
import com.example.navsample.imageanalyzer.ReceiptParser
import com.example.navsample.sorting.NonScrollableGridLayoutManager
import com.example.navsample.sorting.operation.ElementOperationHelper
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import kotlin.math.max

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

    private fun putItemIntoRightList(item: AlgorithmItemAdapterArgument) {
        val userItemAdapterArgument = UserItemAdapterArgument(
            item.value, currentType, item
        )
        if (currentType == Type.PRICE) {
            recycleListUserPrices.add(userItemAdapterArgument)
            userOrderedPricesAdapter.notifyItemInserted(recycleListUserPrices.lastIndex)
        } else {
            recycleListUserNames.add(userItemAdapterArgument)
            userOrderedNamesAdapter.notifyItemInserted(recycleListUserNames.lastIndex)
        }
        item.status = Status.BLOCKED
    }

    private fun configureAlgorithmOneClickAtDefaultMode(
        list: List<AlgorithmItemAdapterArgument>, position: Int
    ) {
        list[position].type = currentType
        putItemIntoRightList(list[position])
        if (sortingMode == SortingElementMode.SWITCHING) {
            changeCurrentType()
        }
    }

    private fun configureAlgorithmOneClick(
        list: List<AlgorithmItemAdapterArgument>, position: Int
    ) {
        if (list[position].type != Type.UNDEFINED) {
            return
        }
        if (editingMode) {
            elementOperationHelper.checkAndUncheckSingleElement(list, position)
        } else {
            configureAlgorithmOneClickAtDefaultMode(list, position)
        }
    }

    private fun configureListAdapter() {
        //ALGORITHM     PRICE
        algorithmOrderedPricesAdapter = AlgorithmItemListAdapter(recycleListAlgorithmPrices,
            { position ->
                configureAlgorithmOneClick(recycleListAlgorithmPrices, position)
                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            }, { position ->
                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            })

        //ALGORITHM     NAME
        algorithmOrderedNamesAdapter = AlgorithmItemListAdapter(recycleListAlgorithmNames,
            { position ->
                configureAlgorithmOneClick(recycleListAlgorithmNames, position)
                algorithmOrderedNamesAdapter.notifyItemChanged(position)
            }, { position ->
                algorithmOrderedNamesAdapter.notifyItemChanged(position)
            })

        //USER     NAME
        userOrderedNamesAdapter = UserItemListAdapter(recycleListUserNames,
            { position ->
                configureUserOneClickAtDefaultMode(
                    recycleListUserNames, position, userOrderedNamesAdapter
                )
            }, { position ->
                configureUserLongClickAtDefaultMode(
                    recycleListUserNames, position, userOrderedNamesAdapter
                )
            })

        //USER     PRICE
        userOrderedPricesAdapter = UserItemListAdapter(recycleListUserPrices,
            { position ->
                configureUserOneClickAtDefaultMode(
                    recycleListUserPrices, position, userOrderedPricesAdapter
                )
            }, { position ->
                configureUserLongClickAtDefaultMode(
                    recycleListUserPrices, position, userOrderedPricesAdapter
                )
            })

        elementOperationHelper = ElementOperationHelper(
            recycleListAlgorithmPrices,
            recycleListAlgorithmNames,
            algorithmOrderedNamesAdapter,
            algorithmOrderedPricesAdapter
        )
    }

    private fun configureUserOneClickAtDefaultMode(
        list: ArrayList<UserItemAdapterArgument>, position: Int, adapter: UserItemListAdapter
    ) {
        if (position >= 0) {
            list[position].algorithmItem.type = Type.UNDEFINED
            list[position].algorithmItem.status = Status.DEFAULT
            refreshAlgPosition(list[position].algorithmItem)
            list.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
    }

    private fun configureUserLongClickAtDefaultMode(
        list: ArrayList<UserItemAdapterArgument>, position: Int, adapter: UserItemListAdapter
    ) {
        if (position >= 0) {
            EditTextDialog(
                list[position].value
            ) { text ->
                list[position].value = text
                list[position].algorithmItem.value = text
                refreshAlgPosition(list[position].algorithmItem)
                adapter.notifyItemChanged(position)
            }.show(childFragmentManager, "TAG")
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
            val namePricePairs =
                prepareListWithGivenSize(max(recycleListUserPrices.size, recycleListUserNames.size))
            recycleListUserNames.forEachIndexed { index, item ->
                namePricePairs[index] += item.value
            }
            recycleListUserPrices.forEachIndexed { index, item ->
                if (namePricePairs[index].isNotEmpty()) {
                    namePricePairs[index] += " "
                }
                namePricePairs[index] += item.value
            }
            receiptDataViewModel.algorithmOrderedNames.value =
                createAlgorithmElementsFromUserElements(recycleListUserNames)
            receiptDataViewModel.algorithmOrderedPrices.value =
                createAlgorithmElementsFromUserElements(recycleListUserPrices)
            receiptDataViewModel.product.value = receiptParser.parseToProducts(namePricePairs)
            Navigation.findNavController(binding.root).popBackStack()
        }
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

    private fun findIndex(
        refObj: AlgorithmItemAdapterArgument, list: List<AlgorithmItemAdapterArgument>
    ): Int {
        for ((index, obj) in list.withIndex()) {
            if (obj === refObj) {
                return index
            }
        }
        return -1
    }

    private fun refreshAlgPosition(item: AlgorithmItemAdapterArgument) {
        val indexInNameList = findIndex(item, algorithmOrderedNamesAdapter.recycleList)
        val indexInPriceList = findIndex(item, algorithmOrderedPricesAdapter.recycleList)
        if (indexInNameList != -1) {
            algorithmOrderedNamesAdapter.notifyItemChanged(indexInNameList)
        }
        if (indexInPriceList != -1) {
            algorithmOrderedPricesAdapter.notifyItemChanged(indexInPriceList)
        }
    }


    private fun prepareListWithGivenSize(size: Int): MutableList<String> {
        val list = mutableListOf<String>()
        for (i in 1..size) {
            list.add("")
        }
        return list
    }

    private fun uncheckAll() {
        elementOperationHelper.executeElementOperation(SortingElementAction.UNCHECK)
    }

    private fun setDefaultStatusAll() {

        elementOperationHelper.executeElementOperation(SortingElementAction.CLEAR_STATUS)
    }

}
