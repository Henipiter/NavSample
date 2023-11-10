package com.example.navsample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.adapters.ExperimentalListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import java.lang.Integer.max
import java.util.Collections

class ExperimentRecycleFragment : Fragment() {
    private var _binding: FragmentExperimentRecycleBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var experimentalListAdapter: ExperimentalListAdapter
    private var recycleList = arrayListOf<String>()

    var sourceItemIndex = -1
    var targetItemIndex = -1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExperimentRecycleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycleList = arrayListOf(
            "01", "02", "03", "04"
        )
        recyclerViewEvent = binding.recyclerViewEventReceipts

        experimentalListAdapter = ExperimentalListAdapter(
            requireContext(),
            recycleList
        )
        recyclerViewEvent.adapter = experimentalListAdapter
        recyclerViewEvent.layoutManager =
            GridLayoutManager(requireContext(), 2)

        binding.addTopButton.setOnClickListener {
            recycleList.add(0, "")
            recycleList.add(0, "")
            experimentalListAdapter.notifyItemInserted(0)
            experimentalListAdapter.notifyItemInserted(1)
        }
        binding.addBottomButton.setOnClickListener {
            recycleList.add("")
            recycleList.add("")
            experimentalListAdapter.notifyItemInserted(recycleList.size - 1)
            experimentalListAdapter.notifyItemInserted(recycleList.size - 2)
        }
        binding.deleteEmptyButton.setOnClickListener {

            val lastOddIndex = max(recycleList.size - recycleList.size % 2 - 1, 0)
            for (i in lastOddIndex downTo 1 step 2) {
                if (recycleList[i - 1] == "" && recycleList[i] == "") {
                    recycleList.removeAt(i)
                    experimentalListAdapter.notifyItemRemoved(i)
                    recycleList.removeAt(i - 1)
                    experimentalListAdapter.notifyItemRemoved(i - 1)
                }
            }
        }
        binding.moveBottomPricesButton.setOnClickListener {
            recycleList.add(0, "")
            recycleList.add("")
            for (i in 1..<recycleList.size step 2) {
                Collections.swap(recycleList, i - 1, i)
            }
            for (i in 1..<recycleList.size step 2) {
                experimentalListAdapter.notifyItemChanged(i)
            }
        }
        binding.moveBottomNamesButton.setOnClickListener {
            recycleList.add(0, "")
            for (i in 2..<recycleList.size step 2) {
                Collections.swap(recycleList, i - 1, i)
            }
            for (i in 0..<recycleList.size step 2) {
                experimentalListAdapter.notifyItemChanged(i)
            }
            recycleList.add("")
            experimentalListAdapter.notifyItemInserted(recycleList.size - 1)
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
//                    sourceItemIndex = viewHolder.adapterPosition
                    targetItemIndex = target.adapterPosition
//                    Collections.swap(recycleList, sourceItemIndex, targetItemIndex)
//                    experimentalListAdapter.notifyItemMoved(sourceItemIndex, targetItemIndex)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    var position = viewHolder.adapterPosition
                    position -= position % 2
                    val pairPosition = position + 1
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            experimentalListAdapter.notifyItemRemoved(position)
                            experimentalListAdapter.notifyItemRemoved(position)
                            recycleList.removeAt(position)
                            recycleList.removeAt(position)
                        }

                        ItemTouchHelper.RIGHT -> {
                            recycleList.set(position, "")
                            experimentalListAdapter.notifyItemChanged(position)
                            if (recycleList.size == pairPosition) {
                                recycleList.add(pairPosition, "")
                                experimentalListAdapter.notifyItemInserted(pairPosition)
                            } else {
                                recycleList.set(pairPosition, "")
                                experimentalListAdapter.notifyItemChanged(pairPosition)
                            }
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
}
