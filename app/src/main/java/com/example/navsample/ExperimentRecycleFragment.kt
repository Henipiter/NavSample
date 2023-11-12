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
import com.example.navsample.DTO.ExperimentalAdapterArgument
import com.example.navsample.adapters.ExperimentalListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import java.lang.Integer.max
import java.util.Collections

class ExperimentRecycleFragment : Fragment() {
    private var _binding: FragmentExperimentRecycleBinding? = null

    private val binding get() = _binding!!

    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var experimentalListAdapter: ExperimentalListAdapter
    private var recycleList = arrayListOf<ExperimentalAdapterArgument>()
    private var checkedElements = arrayListOf<Int>()

//    private var  mainLayout:ConstraintLayout

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
            val listPosition = checkedElements.contains(position)
            if (listPosition) {
                checkedElements.remove(position)
                recycleList.get(position).color = Color.GRAY
            } else {
                checkedElements.add(position)
                recycleList.get(position).color = Color.YELLOW
            }
            experimentalListAdapter.notifyItemChanged(position)
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
        binding.deleteEmptyButton.setOnClickListener {

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
            val indicesDescending = checkedElements.sortedDescending()
            indicesDescending.forEach {
                experimentalListAdapter.recycleList.forEach { Log.d("E", it.value) }
                recycleList.removeAt(it)
                experimentalListAdapter.notifyItemRemoved(it)
                experimentalListAdapter.recycleList.forEach { Log.d("E", it.value) }
            }
            checkedElements.clear()
        }

        binding.mergeButton.setOnClickListener {
            if (checkedElements.isEmpty()) {
                return@setOnClickListener
            }
            var text = ""
            for (i in checkedElements.lastIndex downTo 0) {
                text = recycleList[checkedElements[i]].value + " " + text
            }
            recycleList[checkedElements[0]].value = text
            checkedElements.remove(checkedElements[0])
            experimentalListAdapter.notifyItemChanged(checkedElements[0])
            val indicesDescending = checkedElements.sortedDescending()
            indicesDescending.forEach {
                recycleList.removeAt(it)
                experimentalListAdapter.notifyItemRemoved(it)
            }
            checkedElements.clear()
        }

        binding.swapSelectedButton.setOnClickListener {
            val positionsToSwap = checkedElements.map { it - it % 2 }.distinct()
            uncheckAll()
            positionsToSwap.forEach {
                Collections.swap(recycleList, it, it + 1)
                experimentalListAdapter.notifyItemRangeChanged(it, 2)
            }
        }

        binding.confirmButton.setOnClickListener {
            val namePricePairs = arrayListOf<String>()
            for (i in 0..recycleList.lastIndex step 2) {
                var value = recycleList[i].value
                if (i + 1 > recycleList.lastIndex) {
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

        val touchHelper = ItemTouchHelper(
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
        touchHelper.attachToRecyclerView(recyclerViewEvent)
    }

    private fun uncheckAll() {
        checkedElements.forEach { position ->
            recycleList.get(position).color = Color.GRAY
            experimentalListAdapter.notifyItemChanged(position)
        }
        checkedElements.clear()
    }
}
