package com.example.navsample

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
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
import com.example.navsample.adapters.ExperimentalListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.fragments.EditTextDialog
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
    private var action = Action.NONE
    var sourceItemIndex = -1
    var targetItemIndex = -1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
            requireContext(),
            recycleList
        ) { position ->
            if (productListMode == ProductListMode.SELECT) {
                val listPosition = checkedElements.contains(position)
                if (listPosition) {
                    checkedElements.remove(position)
                    recycleList[position].color = Color.GRAY
                } else {
                    checkedElements.add(position)
                    recycleList[position].color = Color.YELLOW
                }
                experimentalListAdapter.notifyItemChanged(position)
            } else if (productListMode == ProductListMode.EDIT) {
                receiptDataViewModel.experimental.value = recycleList
                EditTextDialog(
                    recycleList.get(position).value
                ) { text ->
                    recycleList.get(position).value = text
                    experimentalListAdapter.notifyItemChanged(position)
                }.show(
                    childFragmentManager, "TAG"
                )
            }

        }

        recyclerViewEvent.adapter = experimentalListAdapter
        recyclerViewEvent.layoutManager =
            GridLayoutManager(requireContext(), 2)

        binding.addTopButton.setOnClickListener {
            recycleList.add(0, ExperimentalAdapterArgument())
            recycleList.add(0, ExperimentalAdapterArgument())
            experimentalListAdapter.notifyItemInserted(0)
            experimentalListAdapter.notifyItemInserted(1)
        }
        binding.swapColumnsButton.setOnClickListener {
            for (i in 1..recycleList.lastIndex step 2) {
                Collections.swap(recycleList, i - 1, i)
            }
            experimentalListAdapter.notifyItemRangeChanged(
                0,
                recycleList.size - recycleList.size % 2
            )

        }
        binding.addBottomButton.setOnClickListener {
            recycleList.add(ExperimentalAdapterArgument())
            recycleList.add(ExperimentalAdapterArgument())
            experimentalListAdapter.notifyItemInserted(recycleList.size - 1)
            experimentalListAdapter.notifyItemInserted(recycleList.size - 2)
        }
        binding.resetButton.setOnClickListener {
            recycleList = receiptDataViewModel.experimentalOriginal.value?.let { it1 ->
                ArrayList(it1)
            } ?: arrayListOf()
            experimentalListAdapter.recycleList = ArrayList(recycleList)
            experimentalListAdapter.notifyDataSetChanged()
        }
        binding.deleteEmptyRowButton.setOnClickListener {

            val lastOddIndex = max(recycleList.size - recycleList.size % 2 - 1, 0)
            for (i in lastOddIndex downTo 1 step 2) {
                if (recycleList[i - 1].value == "" && recycleList[i].value == "") {
                    recycleList.removeAt(i)
                    experimentalListAdapter.notifyItemRemoved(i)
                    recycleList.removeAt(i - 1)
                    experimentalListAdapter.notifyItemRemoved(i - 1)
                }
            }
            if (recycleList.size % 2 == 1) {
                recycleList.add(ExperimentalAdapterArgument())
                experimentalListAdapter.notifyItemInserted(recycleList.size - 1)
            }
        }
        binding.moveBottomPricesButton.setOnClickListener {
            recycleList.add(0, ExperimentalAdapterArgument())
            recycleList.add(ExperimentalAdapterArgument())
            for (i in 1..<recycleList.size step 2) {
                Collections.swap(recycleList, i - 1, i)
            }
            for (i in 1..<recycleList.size step 2) {
                experimentalListAdapter.notifyItemChanged(i)
            }
        }
        binding.moveBottomNamesButton.setOnClickListener {
            recycleList.add(0, ExperimentalAdapterArgument())
            for (i in 2..<recycleList.size step 2) {
                Collections.swap(recycleList, i - 1, i)
            }
            for (i in 0..<recycleList.size step 2) {
                experimentalListAdapter.notifyItemChanged(i)
            }
            recycleList.add(ExperimentalAdapterArgument())
            experimentalListAdapter.notifyItemInserted(recycleList.size - 1)
        }

        binding.unselectAllButton.setOnClickListener {
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
        binding.editButton.setOnClickListener {
            productListMode = ProductListMode.EDIT
            uncheckAll()
            runActionView(EDIT)
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

        binding.applyButton.setOnClickListener {
            execute()
            runButtonsView()
            uncheckAll()
        }
        binding.cancelButton.setOnClickListener {
            runButtonsView()
            uncheckAll()
        }

        val touchHelper = getTouchHelper()
        touchHelper.attachToRecyclerView(recyclerViewEvent)
    }


    private fun runButtonsView() {
        productListMode = ProductListMode.SELECT
        binding.actionLayout.visibility = View.INVISIBLE
        binding.buttonsLayout.visibility = View.VISIBLE
    }

    private fun runActionView(action: Action) {
        this.action = action
        binding.buttonsLayout.visibility = View.INVISIBLE
        binding.actionLayout.visibility = View.VISIBLE
        binding.actionNameText.text = action.toString()
    }

    private fun execute() {
        when (action) {
            DELETE -> {
                val indicesDescending = checkedElements.sortedDescending()
                indicesDescending.forEach {
                    experimentalListAdapter.recycleList.forEach { Log.d("E", it.value) }
                    recycleList.removeAt(it)
                    experimentalListAdapter.notifyItemRemoved(it)
                    experimentalListAdapter.recycleList.forEach { Log.d("E", it.value) }
                }
                checkedElements.clear()
            }

            SWAP -> {
                for (i in 1..checkedElements.lastIndex step 2) {
                    Collections.swap(recycleList, checkedElements[i - 1], checkedElements[i])
                    experimentalListAdapter.notifyItemChanged(checkedElements[i - 1])
                    experimentalListAdapter.notifyItemChanged(checkedElements[i])
                }
            }

            CLEAR -> {
                for (i in checkedElements.lastIndex downTo 0) {
                    recycleList[checkedElements[i]].value = " "
                }
            }

            MERGE -> {
                val firstIndex = checkedElements[0]
                var text = ""
                for (i in checkedElements.lastIndex downTo 0) {
                    text = recycleList[checkedElements[i]].value + " " + text
                }
                recycleList[firstIndex].value = text
                experimentalListAdapter.notifyItemChanged(firstIndex)
                checkedElements.remove(firstIndex)
                val indicesDescending = checkedElements.sortedDescending()
                indicesDescending.forEach {
                    recycleList.removeAt(it)
                    experimentalListAdapter.notifyItemRemoved(it)
                }
                checkedElements.add(firstIndex)
            }

            EDIT -> {}
            NONE -> {}
        }
        this.action = NONE

    }

    private fun uncheckAll() {
        checkedElements.forEach { position ->
            recycleList.get(position).color = Color.GRAY
            experimentalListAdapter.notifyItemChanged(position)
        }
        checkedElements.clear()
    }

    private fun getTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(
            object : ItemTouchHelper.Callback() {
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return makeMovementFlags(
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    )
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    targetItemIndex = target.adapterPosition
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    var position = viewHolder.adapterPosition
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            position -= position % 2
                            experimentalListAdapter.notifyItemRemoved(position)
                            experimentalListAdapter.notifyItemRemoved(position)
                            recycleList.removeAt(position)
                            recycleList.removeAt(position)
                        }

                        ItemTouchHelper.RIGHT -> {
                            recycleList.set(position, ExperimentalAdapterArgument())
                            experimentalListAdapter.notifyItemChanged(position)
                        }
                    }
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    when (actionState) {
                        ItemTouchHelper.ACTION_STATE_DRAG -> {
                            sourceItemIndex = viewHolder?.adapterPosition ?: -1
                        }

                        ItemTouchHelper.ACTION_STATE_IDLE -> {
                            if (sourceItemIndex != -1 && targetItemIndex != -1
                                && sourceItemIndex != targetItemIndex
                            ) {
                                moveItem(sourceItemIndex, targetItemIndex);
                                sourceItemIndex = -1;
                                targetItemIndex = -1;
                            }

                        }
                    }
                }

                private fun moveItem(oldPos: Int, newPos: Int) {
                    val temp = recycleList.get(oldPos)
                    recycleList.set(oldPos, recycleList.get(newPos))
                    recycleList.set(newPos, temp)
                    experimentalListAdapter.notifyItemChanged(oldPos)
                    experimentalListAdapter.notifyItemChanged(newPos)
                }

            }

        )

    }
}
