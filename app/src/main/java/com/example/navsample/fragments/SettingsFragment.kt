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
import com.example.navsample.auth.AccountServiceImpl
import com.example.navsample.databinding.FragmentSettingsBinding
import com.example.navsample.entities.init.InitDatabaseHelper
import com.example.navsample.viewmodels.fragment.AddCategoryDataViewModel
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel
import com.example.navsample.viewmodels.fragment.AddReceiptDataViewModel
import com.example.navsample.viewmodels.fragment.AddStoreDataViewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!
    private val addStoreDataViewModel: AddStoreDataViewModel by activityViewModels()
    private val addCategoryDataViewModel: AddCategoryDataViewModel by activityViewModels()
    private val addReceiptDataViewModel: AddReceiptDataViewModel by activityViewModels()
    private val addProductDataViewModel: AddProductDataViewModel by activityViewModels()


    companion object {
        private const val FILLED_DB = "filled_db"
    }

    private lateinit var myPref: SharedPreferences
    private var accountServiceImpl = AccountServiceImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logInButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_signingPanelFragment)
        }
        binding.exportDataButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_exportDataFragment)
        }
        if (BuildConfig.DEVELOPER) {
            devButton()
        }
        myPref =
            requireContext().getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
        if (myPref.getString(FILLED_DB, "false") == "false") {
//            initDatabase()
            myPref.edit().putString(FILLED_DB, "true").apply()
        }
    }

    private fun initDatabase() {
        InitDatabaseHelper.getStores().forEach { store ->
            addStoreDataViewModel.insertStore(store, false)
        }
        InitDatabaseHelper.getCategories().forEach { category ->
            addCategoryDataViewModel.insertCategory(category, false)
        }
        InitDatabaseHelper.getReceipts().forEach { receipt ->
            addReceiptDataViewModel.insertReceipt(receipt, false)
        }
        InitDatabaseHelper.getProducts().forEach { product ->
            addProductDataViewModel.insertProducts(product)
        }
    }

    private fun devButton() {
        binding.developmentLayout.visibility = View.VISIBLE
        binding.recycleViewTest.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_experimentRecycleFragment)
        }
        binding.guideTest.setOnClickListener {
            val intent = Intent(requireContext(), GuideActivity::class.java)
            startActivity(intent)
        }
    }
}
