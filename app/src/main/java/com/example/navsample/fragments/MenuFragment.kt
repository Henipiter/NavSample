package com.example.navsample.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.navsample.BuildConfig
import com.example.navsample.R
import com.example.navsample.activity.GuideActivity
import com.example.navsample.databinding.FragmentMenuBinding
import com.example.navsample.entities.init.InitDatabaseHelper
import com.example.navsample.viewmodels.ReceiptDataViewModel
import com.example.navsample.viewmodels.ReceiptImageViewModel
import java.util.UUID

class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null

    private val binding get() = _binding!!
    private val receiptDataViewModel: ReceiptDataViewModel by activityViewModels()
    private val receiptImageViewModel: ReceiptImageViewModel by activityViewModels()


    companion object {
        private const val FILLED_DB = "filled_db"
    }

    private lateinit var myPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.addReceipt.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_imageImportFragment)
        }
        binding.showReceipt.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_listingFragment)
        }
        binding.exportDataButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_exportDataFragment)
        }
        binding.diagramView.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_diagramFragment)
        }
        receiptDataViewModel.clearData()
        receiptImageViewModel.clearData()

        val myUuid = UUID.randomUUID()
        receiptDataViewModel.imageUuid.value = myUuid.toString()
        receiptImageViewModel.uid.value = myUuid.toString()

        if (BuildConfig.DEVELOPER) {
            devButton()
        }

        myPref = requireContext().getSharedPreferences(
            "preferences",
            AppCompatActivity.MODE_PRIVATE
        )
        if (myPref.getString(FILLED_DB, "false") == "false") {
            initDatabase()
            myPref.edit().putString(FILLED_DB, "true").apply()
        } else {
            receiptDataViewModel.setUserUuid()
        }
    }

    private fun initDatabase() {
        observeUserUUid()
    }

    private fun devButton() {
        binding.developmentLayout.visibility = View.VISIBLE
        binding.recycleViewTest.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_menuFragment_to_experimentRecycleFragment)
        }
        binding.guideTest.setOnClickListener {
            val intent = Intent(requireContext(), GuideActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeUserUUid() {
        receiptDataViewModel.setUserUuid()
        receiptDataViewModel.userUuid.observe(viewLifecycleOwner) {
            if (it != null && it != "") {
                InitDatabaseHelper.getStores().forEach { store ->
                    receiptDataViewModel.insertStore(store)
                }
                InitDatabaseHelper.getCategories().forEach { category ->
                    receiptDataViewModel.insertCategoryList(category)
                }
                InitDatabaseHelper.getReceipts().forEach { receipt ->
                    receiptDataViewModel.insertReceipt(receipt)
                }
                InitDatabaseHelper.getProducts().forEach { product ->
                    receiptDataViewModel.insertProducts(product)
                }
                receiptDataViewModel.userUuid.removeObserver { }
            }
        }
    }
}
