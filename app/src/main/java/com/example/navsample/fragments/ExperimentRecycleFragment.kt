@file:Suppress("KotlinConstantConditions")

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
import com.example.navsample.adapters.AlgorithmItemListAdapter
import com.example.navsample.adapters.UserItemListAdapter
import com.example.navsample.databinding.FragmentExperimentRecycleBinding
import com.example.navsample.dto.Action
import com.example.navsample.dto.Action.CLEAR
import com.example.navsample.dto.Action.DELETE
import com.example.navsample.dto.Action.EDIT
import com.example.navsample.dto.Action.MERGE
import com.example.navsample.dto.Action.NONE
import com.example.navsample.dto.Action.SWAP
import com.example.navsample.dto.AlgorithmItemAdapterArgument
import com.example.navsample.dto.SortingElementMode
import com.example.navsample.dto.Status
import com.example.navsample.dto.Type
import com.example.navsample.dto.UserItemAdapterArgument
import com.example.navsample.fragments.dialogs.EditTextDialog
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
    private var checkedElementsCounter = 0
    private lateinit var receiptParser: ReceiptParser
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

            else -> {
                currentType = Type.NAME
                binding.priceModeColor.visibility = View.INVISIBLE
                binding.nameModeColor.visibility = View.VISIBLE
            }
        }
    }

    private fun changeCurrentType() {
        when (currentType) {
            Type.PRICE -> {
                currentType = Type.NAME
                binding.priceModeColor.visibility = View.INVISIBLE
                binding.nameModeColor.visibility = View.VISIBLE
            }

            else -> {
                currentType = Type.PRICE
                binding.priceModeColor.visibility = View.VISIBLE
                binding.nameModeColor.visibility = View.INVISIBLE
            }
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

    private fun configureAlgorithmOneClickAtEditingMode(
        list: List<AlgorithmItemAdapterArgument>, position: Int
    ) {
        if (list[position].status == Status.DEFAULT) {
            list[position].status = Status.CHOSEN
            list[position].number = checkedElementsCounter
            checkedElementsCounter += 1
        } else if (list[position].status == Status.CHOSEN) {
            list[position].status = Status.DEFAULT
            decrementNumberInCells(list[position].number)
            list[position].number = 0
            checkedElementsCounter -= 1
        }
    }

    private fun decrementNumberInCells(limit: Int) {
        recycleListAlgorithmNames.forEachIndexed { position, item ->
            if (item.number > limit) {
                item.number -= 1
                algorithmOrderedNamesAdapter.notifyItemChanged(position)
            }
        }
        recycleListAlgorithmPrices.forEachIndexed { position, item ->
            if (item.number > limit) {
                item.number -= 1
                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            }
        }

    }

    private fun configureAlgorithmOneClick(
        list: List<AlgorithmItemAdapterArgument>, position: Int
    ) {
        if (list[position].type != Type.UNDEFINED) {
            return
        }
        if (editingMode) {
            configureAlgorithmOneClickAtEditingMode(list, position)
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
        userOrderedNamesAdapter = UserItemListAdapter(requireContext(), recycleListUserNames,
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
        userOrderedPricesAdapter = UserItemListAdapter(requireContext(), recycleListUserPrices,
            { position ->
                configureUserOneClickAtDefaultMode(
                    recycleListUserPrices, position, userOrderedPricesAdapter
                )
            }, { position ->
                configureUserLongClickAtDefaultMode(
                    recycleListUserPrices, position, userOrderedPricesAdapter
                )
            })

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
            runActionView(DELETE)
        }

        binding.clearButton.setOnClickListener {
            runActionView(CLEAR)
        }
        binding.mergeButton.setOnClickListener {
            runActionView(MERGE)
        }
        binding.swapSelectedButton.setOnClickListener {
            runActionView(SWAP)
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
            namePricePairs.forEach { Log.d("EEEE", it) }
            receiptDataViewModel.product.value = receiptParser.parseToProducts(namePricePairs)
            Navigation.findNavController(binding.root).popBackStack()
        }
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

    private fun runActionView(action: Action) {
        if (checkedElementsCounter > 0) {
            execute(action)
        }
//        uncheckAll()
    }

    private fun execute(action: Action) {
        when (action) {
            DELETE -> {
                for (i in recycleListAlgorithmPrices.lastIndex downTo 0) {
                    if (recycleListAlgorithmPrices[i].status != Status.CHOSEN) {
                        continue
                    }
                    recycleListAlgorithmPrices.removeAt(i)
                    algorithmOrderedPricesAdapter.notifyItemRemoved(i)
                }
                for (i in recycleListAlgorithmNames.lastIndex downTo 0) {
                    if (recycleListAlgorithmNames[i].status != Status.CHOSEN) {
                        continue
                    }
                    recycleListAlgorithmNames.removeAt(i)
                    algorithmOrderedNamesAdapter.notifyItemRemoved(i)
                }
                checkedElementsCounter = 0
            }

            SWAP -> {
                val valueList = prepareListWithGivenSize(checkedElementsCounter)
                //reading values
                recycleListAlgorithmPrices.forEach {
                    if (it.status != Status.CHOSEN) {
                        return@forEach
                    }
                    valueList[it.number] = it.value
                }
                recycleListAlgorithmNames.forEach {
                    if (it.status != Status.CHOSEN) {
                        return@forEach
                    }
                    valueList[it.number] = it.value
                }
                //writing values
                recycleListAlgorithmPrices.forEachIndexed { position, it ->
                    if (it.status != Status.CHOSEN) {
                        return@forEachIndexed
                    }
                    if (checkedElementsCounter % 2 == 1 && it.number == checkedElementsCounter - 1) {
                        it.number = -1
                        it.status = Status.DEFAULT
                        algorithmOrderedPricesAdapter.notifyItemChanged(position)
                    } else if (it.number >= 0) {
                        val oppositeIndex = if (it.number % 2 == 0) {
                            it.number + 1
                        } else {
                            it.number - 1
                        }
                        it.value = valueList[oppositeIndex]
                        it.number = -1
                        it.status = Status.DEFAULT
                        algorithmOrderedPricesAdapter.notifyItemChanged(position)
                    }

                }
                recycleListAlgorithmNames.forEachIndexed { position, it ->
                    if (it.status != Status.CHOSEN) {
                        return@forEachIndexed
                    }
                    if (checkedElementsCounter % 2 == 1 && it.number == checkedElementsCounter - 1) {
                        it.number = -1
                        it.status = Status.DEFAULT
                        algorithmOrderedNamesAdapter.notifyItemChanged(position)
                    } else if (it.number >= 0) {
                        val oppositeIndex = if (it.number % 2 == 0) {
                            it.number + 1
                        } else {
                            it.number - 1
                        }
                        it.value = valueList[oppositeIndex]
                        it.number = -1
                        it.status = Status.DEFAULT
                        algorithmOrderedNamesAdapter.notifyItemChanged(position)
                    }
                }
                checkedElementsCounter = 0
            }

            CLEAR -> {
                recycleListAlgorithmPrices.forEachIndexed { position, it ->
                    if (it.status != Status.CHOSEN) {
                        return@forEachIndexed
                    }
                    it.value = ""
                    it.number = -1
                    it.status = Status.DEFAULT
                    algorithmOrderedPricesAdapter.notifyItemChanged(position)

                }
                recycleListAlgorithmNames.forEachIndexed { position, it ->
                    if (it.status != Status.CHOSEN) {
                        return@forEachIndexed
                    }
                    it.value = ""
                    it.number = -1
                    it.status = Status.DEFAULT
                    algorithmOrderedNamesAdapter.notifyItemChanged(position)

                }
                checkedElementsCounter = 0

            }

            MERGE -> {
                val valueList = prepareListWithGivenSize(checkedElementsCounter)
                var firstElement: AlgorithmItemAdapterArgument? = null
                var firstElementPosition = -1
                var firstElementListType = Type.UNDEFINED

                recycleListAlgorithmPrices.forEachIndexed { position, it ->
                    if (it.status != Status.CHOSEN) {
                        return@forEachIndexed
                    }
                    if (it.number == 0) {
                        valueList[0] = it.value
                        it.number = -1
                        it.status = Status.DEFAULT
                        firstElement = it
                        firstElementPosition = position
                        firstElementListType = Type.PRICE
                    }
                    if (it.number > 0) {
                        valueList[it.number] = it.value
                    }
                }

                recycleListAlgorithmNames.forEachIndexed { position, it ->
                    if (it.status != Status.CHOSEN) {
                        return@forEachIndexed
                    }
                    if (it.number == 0) {
                        valueList[0] = it.value
                        it.number = -1
                        it.status = Status.DEFAULT
                        firstElement = it
                        firstElementPosition = position
                        firstElementListType = Type.NAME
                    }
                    if (it.number > 0) {
                        valueList[it.number] = it.value
                    }
                }

                if (firstElement == null) {
                    throw Exception("Can't find first element")
                }
                firstElement?.let {
                    it.value = valueList.joinToString(separator = " ")
                    if (firstElementListType == Type.NAME) {
                        algorithmOrderedNamesAdapter.notifyItemChanged(firstElementPosition)
                    }
                    if (firstElementListType == Type.PRICE) {
                        algorithmOrderedPricesAdapter.notifyItemChanged(firstElementPosition)
                    }
                }
                for (i in recycleListAlgorithmNames.lastIndex downTo 0) {
                    if (recycleListAlgorithmNames[i].status == Status.CHOSEN && recycleListAlgorithmNames[i].number > 0) {
                        recycleListAlgorithmNames.removeAt(i)
                        algorithmOrderedNamesAdapter.notifyItemRemoved(i)
                    }
                }
                for (i in recycleListAlgorithmPrices.lastIndex downTo 0) {
                    if (recycleListAlgorithmPrices[i].status == Status.CHOSEN && recycleListAlgorithmPrices[i].number > 0) {
                        recycleListAlgorithmPrices.removeAt(i)
                        algorithmOrderedPricesAdapter.notifyItemRemoved(i)
                    }
                }
                checkedElementsCounter = 0
            }

            EDIT -> {}
            NONE -> {}
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
        checkedElementsCounter = 0
        recycleListAlgorithmPrices.forEachIndexed { position, item ->
            if (item.number >= 0) {
                item.status = Status.DEFAULT
                item.number = -1
                algorithmOrderedPricesAdapter.notifyItemChanged(position)
            }
        }
        recycleListAlgorithmNames.forEachIndexed { position, item ->
            if (item.number >= 0) {
                item.status = Status.DEFAULT
                item.number = -1
                algorithmOrderedNamesAdapter.notifyItemChanged(position)
            }
        }
    }

    private fun setDefaultStatusAll() {
        recycleListAlgorithmPrices.forEachIndexed { position, item ->
            item.status = Status.DEFAULT
            item.number = -1
            item.type = Type.UNDEFINED
            algorithmOrderedPricesAdapter.notifyItemChanged(position)

        }
        recycleListAlgorithmNames.forEachIndexed { position, item ->
            item.status = Status.DEFAULT
            item.number = -1
            item.type = Type.UNDEFINED
            algorithmOrderedNamesAdapter.notifyItemChanged(position)
        }
    }

}
