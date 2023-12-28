package com.example.navsample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.DTO.Action
import com.example.navsample.DTO.Action.CLEAR
import com.example.navsample.DTO.Action.DELETE
import com.example.navsample.DTO.Action.EDIT
import com.example.navsample.DTO.Action.MERGE
import com.example.navsample.DTO.Action.NONE
import com.example.navsample.DTO.Action.SWAP
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.DTO.ProductListMode
import com.example.navsample.ReceiptParser
import com.example.navsample.adapters.ExperimentalListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.fragments.dialogs.EditTextDialog
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import java.lang.Integer.max
import java.util.Collections

open class ExperimentRecycleFragment : Fragment() {
    private var _binding: FragmentExperimentRecycleBinding? = null

    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var experimentalListAdapter: ExperimentalListAdapter
    private var recycleList = arrayListOf<ExperimentalAdapterArgument>()
    private var checkedElements = arrayListOf<Int>()

    private var productListMode = ProductListMode.SELECT
    private var action = NONE

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        recycleList = receiptDataViewModel.experimental.value ?: arrayListOf()
        recyclerViewEvent = binding.recyclerViewEventReceipts

        experimentalListAdapter = ExperimentalListAdapter(
            recycleList,
            { position ->
                val listPosition = checkedElements.contains(position)
                if (listPosition) {
                    val positionAtList = checkedElements.indexOf(position)
                    checkedElements.remove(position)
                    for (i in positionAtList..checkedElements.lastIndex) {
                        recycleList[checkedElements[i]].number -= 1
                        experimentalListAdapter.notifyItemChanged(checkedElements[i])
                    }
                    recycleList[position].chosen = false
                    recycleList[position].number = 0
                    experimentalListAdapter.notifyItemChanged(position)
                } else {
                    checkedElements.add(position)
                    recycleList[position].chosen = true
                    recycleList[position].number = checkedElements.size
                    experimentalListAdapter.notifyItemChanged(position)
                }
            }, { position ->
                receiptDataViewModel.experimental.value = recycleList
                val newList = recycleList.toMutableList()
                EditTextDialog(
                    recycleList[position].value,
                    {
                        val pos = position - position % 2
                        newList.add(pos, ExperimentalAdapterArgument())
                        newList.add(pos, ExperimentalAdapterArgument())
                        experimentalListAdapter.setNewData(newList)
                    },
                    {
                        val pos = position + (position + 1) % 2 + 1
                        newList.add(pos, ExperimentalAdapterArgument())
                        newList.add(pos, ExperimentalAdapterArgument())

                        experimentalListAdapter.setNewData(newList)
                    },
                    { text ->
                        recycleList[position].value = text
                        experimentalListAdapter.notifyItemChanged(position)
                    }).show(childFragmentManager, "TAG")
            })

        recyclerViewEvent.adapter = experimentalListAdapter
        recyclerViewEvent.layoutManager = GridLayoutManager(requireContext(), 2)


        binding.addTopButton.setOnClickListener {
            val newList = recycleList.toMutableList()
            newList.add(0, ExperimentalAdapterArgument())
            newList.add(0, ExperimentalAdapterArgument())
            experimentalListAdapter.setNewData(newList)
        }
        binding.swapColumnsButton.setOnClickListener {
            val newList = recycleList.toMutableList()
            for (i in 1..newList.lastIndex step 2) {
                Collections.swap(newList, i - 1, i)
            }
            experimentalListAdapter.setNewData(newList)
        }
        binding.addBottomButton.setOnClickListener {
            val newList = recycleList.toMutableList()
            newList.add(ExperimentalAdapterArgument())
            newList.add(ExperimentalAdapterArgument())
            experimentalListAdapter.setNewData(newList)
        }
        binding.resetButton.setOnClickListener {

            val newList = receiptDataViewModel.experimentalOriginal.value?.let { it1 ->
                ArrayList(it1)
            } ?: arrayListOf()
            experimentalListAdapter.setNewData(newList)
            uncheckAll()
        }
        binding.deleteEmptyRowButton.setOnClickListener {

            val newList = recycleList.toMutableList()
            val lastOddIndex = max(newList.size - newList.size % 2 - 1, 0)
            for (i in lastOddIndex downTo 1 step 2) {
                if (newList[i - 1].value == "" && newList[i].value == "") {
                    newList.removeAt(i)
                    newList.removeAt(i - 1)
                }
            }
            if (newList.size % 2 == 1) {
                newList.add(ExperimentalAdapterArgument())
            }
            experimentalListAdapter.setNewData(newList)
        }
        binding.moveBottomPricesButton.setOnClickListener {
            val newList = recycleList.toMutableList()
            newList.add(0, ExperimentalAdapterArgument())
            newList.add(ExperimentalAdapterArgument())
            for (i in 1..<newList.size step 2) {
                Collections.swap(newList, i - 1, i)
            }
            experimentalListAdapter.setNewData(newList)
        }
        binding.moveBottomNamesButton.setOnClickListener {

            val newList = recycleList.toMutableList()
            newList.add(0, ExperimentalAdapterArgument())
            for (i in 2..<newList.size step 2) {
                Collections.swap(newList, i - 1, i)
            }
            newList.add(ExperimentalAdapterArgument())
            experimentalListAdapter.setNewData(newList)
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
            for (i in 0..recycleList.lastIndex step 2) {
                var value = recycleList[i].value
                if (i + 1 <= recycleList.lastIndex) {
                    value += recycleList[i + 1].value
                }
                if (value != "") {
                    namePricePairs.add(value)
                }
            }
            namePricePairs.forEach { Log.d("ImageProcess", it) }
            val receiptParser = ReceiptParser()
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
        when (action) {
            DELETE -> {
                val leftSide =
                    recycleList.filterIndexed { index, _ -> index % 2 == 0 }.toMutableList()
                val rightSide =
                    recycleList.filterIndexed { index, _ -> index % 2 == 1 }.toMutableList()

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
                experimentalListAdapter.setNewData(newList)
                checkedElements.clear()
            }

            SWAP -> {
                val newList = recycleList.toMutableList()
                for (i in 1..checkedElements.lastIndex step 2) {
                    Collections.swap(newList, checkedElements[i - 1], checkedElements[i])
                }
                experimentalListAdapter.setNewData(newList)
            }

            CLEAR -> {
                val newList = recycleList.toMutableList()
                for (i in checkedElements.lastIndex downTo 0) {
                    newList[checkedElements[i]] = ExperimentalAdapterArgument(" ")
                }
                experimentalListAdapter.setNewData(newList)
            }

            MERGE -> {
                val newList = recycleList.toMutableList()
                val firstIndex = checkedElements[0]
                var text = ""
                for (i in checkedElements.lastIndex downTo 0) {
                    text = newList[checkedElements[i]].value + " " + text
                }
                newList[firstIndex] = ExperimentalAdapterArgument(text)
                checkedElements.remove(firstIndex)
                val indicesDescending = checkedElements.sortedDescending()
                indicesDescending.forEach {
                    newList.removeAt(it)
                }
                experimentalListAdapter.setNewData(newList)
                checkedElements.clear()
            }

            EDIT -> {}
            NONE -> {}
        }
        this.action = NONE

    }

    private fun uncheckAll() {
        val newList = recycleList.toMutableList()
        checkedElements.forEach { position ->
            newList[position] = ExperimentalAdapterArgument(newList[position])
            newList[position].chosen = false
            newList[position].number = 0
        }
        experimentalListAdapter.setNewData(newList)
        checkedElements.clear()
    }
}
