package com.example.navsample.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.InitDatabaseViewModel
import com.example.navsample.viewmodels.fragment.AddCategoryDataViewModel
import com.example.navsample.viewmodels.fragment.AddProductDataViewModel
import com.example.navsample.viewmodels.fragment.AddReceiptDataViewModel
import com.example.navsample.viewmodels.fragment.AddStoreDataViewModel
import java.util.UUID

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!
    private val initDatabaseViewModel: InitDatabaseViewModel by activityViewModels()
    private val addStoreDataViewModel: AddStoreDataViewModel by activityViewModels()
    private val addCategoryDataViewModel: AddCategoryDataViewModel by activityViewModels()
    private val addReceiptDataViewModel: AddReceiptDataViewModel by activityViewModels()
    private val addProductDataViewModel: AddProductDataViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()


    companion object {
        private const val FILLED_DB = "filled_db"
        private const val USER_ID = "userId"
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
            Toast.makeText(
                requireContext(),
                "EEE'${accountServiceImpl.currentUserId}'EEE",
                Toast.LENGTH_SHORT
            ).show()
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_signingPanelFragment)

        }
        binding.exportDataButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_exportDataFragment)
        }
        imageViewModel.clearData()

        val myUuid = UUID.randomUUID()
        initDatabaseViewModel.imageUuid.value = myUuid.toString()
        imageViewModel.uid.value = myUuid.toString()

        if (BuildConfig.DEVELOPER) {
            devButton()
        }
        myPref =
            requireContext().getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
        if (myPref.getString(USER_ID, "") == "") {
            myPref.edit().putString(USER_ID, UUID.randomUUID().toString()).apply()
        }
        if (myPref.getString(FILLED_DB, "false") == "false") {
            initDatabase()
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
