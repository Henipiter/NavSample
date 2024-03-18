package com.example.navsample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.NonScrollableGridLayoutManager
import com.example.navsample.ReceiptParser
import com.example.navsample.adapters.ExperimentalListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.dto.Action
import com.example.navsample.dto.Action.CLEAR
import com.example.navsample.dto.Action.DELETE
import com.example.navsample.dto.Action.EDIT
import com.example.navsample.dto.Action.MERGE
import com.example.navsample.dto.Action.NONE
import com.example.navsample.dto.Action.SWAP
import com.example.navsample.dto.ExperimentalAdapterArgument
import com.example.navsample.dto.ProductListMode
import com.example.navsample.dto.Type
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import java.lang.Integer.max
import java.util.Collections

open class ExperimentRecycleFragment : Fragment() {
    private var _binding: FragmentExperimentRecycleBinding? = null

    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var algorithmOrderedPricesAdapter: ExperimentalListAdapter
    private lateinit var algorithmOrderedNamesAdapter: ExperimentalListAdapter
    private var recycleListPrices = arrayListOf<ExperimentalAdapterArgument>()
    private var recycleListNames = arrayListOf<ExperimentalAdapterArgument>()
    private var checkedElementsCounter = 0
    private lateinit var receiptParser: ReceiptParser
    private var productListMode = ProductListMode.SELECT
    private var action = NONE//recycler_view_event_receipts


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

    private fun changeCurrentType() {
        currentType = when (currentType) {
            Type.PRICE -> Type.NAME
            else -> {
                Type.PRICE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        receiptParser = ReceiptParser(
            receiptDataViewModel.receipt.value?.id ?: -1,            //throw NoReceiptIdException(),
            receiptDataViewModel.store.value?.defaultCategoryId ?: -1 // throw NoStoreIdException()
        )
        recycleListNames = receiptDataViewModel.algorithmOrderedNames.value ?: arrayListOf()
        recycleListPrices = receiptDataViewModel.algorithmOrderedPrices.value ?: arrayListOf()
        algorithmOrderedPricesAdapter = ExperimentalListAdapter(
            recycleListPrices,
            { position ->
                if (recycleListPrices[position].type == Type.UNDEFINED) {
                    recycleListPrices[position].type = currentType
                    changeCurrentType()
                } else {
                    recycleListPrices[position].type = Type.UNDEFINED
                }
                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            }, { position ->

                recycleListPrices[position].chosen = !recycleListPrices[position].chosen
                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            })
        algorithmOrderedNamesAdapter = ExperimentalListAdapter(
            recycleListNames,
            { position ->
                if (recycleListNames[position].type == Type.UNDEFINED) {
                    recycleListNames[position].type = currentType
                    changeCurrentType()
                } else {
                    recycleListNames[position].type = Type.UNDEFINED
                }
                algorithmOrderedNamesAdapter.notifyItemChanged(position)

            }, { position ->

                recycleListNames[position].chosen = !recycleListNames[position].chosen
                algorithmOrderedNamesAdapter.notifyItemChanged(position)
            })


        binding.recyclerViewThree.adapter = algorithmOrderedPricesAdapter
        binding.recyclerViewThree.layoutManager =
            NonScrollableGridLayoutManager(requireContext(), 1)
        binding.recyclerViewFour.adapter = algorithmOrderedNamesAdapter
        binding.recyclerViewFour.layoutManager = NonScrollableGridLayoutManager(requireContext(), 1)

        binding.resetButton.setOnClickListener {

            val newPricesList = receiptDataViewModel.algorithmOrderedPrices.value?.let {
                ArrayList(it)
            } ?: arrayListOf()
            algorithmOrderedPricesAdapter.setNewData(newPricesList)
            val newNamesList = receiptDataViewModel.algorithmOrderedNames.value?.let {
                ArrayList(it)
            } ?: arrayListOf()
            algorithmOrderedNamesAdapter.setNewData(newNamesList)

            uncheckAll()
        }

        binding.unselectAllButton2.setOnClickListener {
            uncheckAll()
        }

        binding.deleteButton.setOnClickListener {
            productListMode = ProductListMode.SELECT
            runActionView(DELETE)
        }

        binding.clearButton.setOnClickListener {
            productListMode = ProductListMode.SELECT
            runActionView(CLEAR)
        }
        binding.mergeButton.setOnClickListener {
            productListMode = ProductListMode.SELECT
            runActionView(MERGE)
        }
        binding.swapSelectedButton.setOnClickListener {
            productListMode = ProductListMode.SELECT
            runActionView(SWAP)
        }

        binding.confirmButton.setOnClickListener {
            val namePricePairs = arrayListOf<String>()
            for (i in 0..recycleListPrices.lastIndex step 2) {
                var value = recycleListPrices[i].value
                if (i + 1 <= recycleListPrices.lastIndex) {
                    value += " " + recycleListPrices[i + 1].value
                }
                if (value != "") {
                    namePricePairs.add(value)
                }
            }
            namePricePairs.forEach { Log.d("ImageProcess", it) }
            receiptDataViewModel.product.value = receiptParser.parseToProducts(namePricePairs)
            Navigation.findNavController(binding.root).popBackStack()
        }
    }

    private fun runActionView(action: Action) {
        this.action = action
        execute()
        uncheckAll()
    }

    private fun execute() {
        val checkedElements = getIndexOfChoseItems()
        when (action) {
            DELETE -> {
                val leftSide =
                    recycleListPrices.filterIndexed { index, _ -> index % 2 == 0 }.toMutableList()
                val rightSide =
                    recycleListPrices.filterIndexed { index, _ -> index % 2 == 1 }.toMutableList()

                val indicesDescending = checkedElements.sortedDescending()
                val newList = mutableListOf<ExperimentalAdapterArgument>()

                indicesDescending.forEach {
                    if (it % 2 == 0) {
                        leftSide.removeAt(it / 2)
                    } else {
                        rightSide.removeAt(it / 2)
                    }
                }
                for (i in 0..max(leftSide.lastIndex, rightSide.lastIndex)) {
                    if (i <= leftSide.lastIndex) {
                        newList.add(leftSide[i])
                    } else {
                        newList.add(ExperimentalAdapterArgument())
                    }
                    if (i <= rightSide.lastIndex) {
                        newList.add(rightSide[i])
                    } else {
                        newList.add(ExperimentalAdapterArgument())
                    }
                }
                algorithmOrderedPricesAdapter.setNewData(newList)
            }

            SWAP -> {
                val newList = recycleListPrices.toMutableList()
                for (i in 1..checkedElements.lastIndex step 2) {
                    Collections.swap(newList, checkedElements[i - 1], checkedElements[i])
                }
                algorithmOrderedPricesAdapter.setNewData(newList)
            }

            CLEAR -> {
                val newList = recycleListPrices.toMutableList()
                for (i in checkedElements.lastIndex downTo 0) {
                    newList[checkedElements[i]] =
                        ExperimentalAdapterArgument(" ", newList[checkedElements[i]].type)
                }
                algorithmOrderedPricesAdapter.setNewData(newList)
            }

            MERGE -> {
                val newList = recycleListPrices.toMutableList()
                val firstIndex = checkedElements[0]
                var text = ""
                for (i in checkedElements.lastIndex downTo 0) {
                    text = newList[checkedElements[i]].value + " " + text
                }
                newList[firstIndex] = ExperimentalAdapterArgument(text, newList[firstIndex].type)
                checkedElements.remove(firstIndex)
                val indicesDescending = checkedElements.sortedDescending()
                indicesDescending.forEach {
                    newList.removeAt(it)
                }
                algorithmOrderedPricesAdapter.setNewData(newList)
            }

            EDIT -> {}
            NONE -> {}
        }
        this.action = NONE

    }

    private fun uncheckAll() {
        checkedElementsCounter = 0
        val newList = arrayListOf<ExperimentalAdapterArgument>()
        recycleListPrices.forEach { item ->
            val argument = ExperimentalAdapterArgument(item)
            argument.chosen = false
            argument.number = 0
            newList.add(argument)
        }
        algorithmOrderedPricesAdapter.setNewData(newList)
    }

    private fun getIndexOfChoseItems(): ArrayList<Int> {
        val list = arrayListOf<Int>()
        for (i in 1..checkedElementsCounter) {
            list.add(0)
        }
        for (i in 0..recycleListPrices.lastIndex) {
            val currentNumber = recycleListPrices[i].number
            if (currentNumber > 0) {
                list[currentNumber - 1] = i
            }
        }
        return list
    }
}
