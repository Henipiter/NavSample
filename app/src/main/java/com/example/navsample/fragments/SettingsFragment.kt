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
import com.example.navsample.databinding.FragmentSettingsBinding
import com.example.navsample.entities.init.InitDatabaseHelper
import com.example.navsample.viewmodels.ImageViewModel
import com.example.navsample.viewmodels.InitDatabaseViewModel
import java.util.UUID

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!
    private val initDatabaseViewModel: InitDatabaseViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()


    companion object {
        private const val FILLED_DB = "filled_db"
    }

    private lateinit var myPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showReceipt.setOnClickListener {
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

        myPref = requireContext().getSharedPreferences(
            "preferences",
            AppCompatActivity.MODE_PRIVATE
        )
        if (myPref.getString(FILLED_DB, "false") == "false") {
            initDatabase()
            myPref.edit().putString(FILLED_DB, "true").apply()
        } else {
            initDatabaseViewModel.setUserUuid()
        }
    }

    private fun initDatabase() {
        observeUserUUid()
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

    private fun observeUserUUid() {
        initDatabaseViewModel.setUserUuid()
        initDatabaseViewModel.userUuid.observe(viewLifecycleOwner) {
            if (it != null && it != "") {
                InitDatabaseHelper.getStores().forEach { store ->
                    initDatabaseViewModel.insertStore(store)
                }
                InitDatabaseHelper.getCategories().forEach { category ->
                    initDatabaseViewModel.insertCategoryList(category)
                }
                InitDatabaseHelper.getReceipts().forEach { receipt ->
                    initDatabaseViewModel.insertReceipt(receipt)
                }
                InitDatabaseHelper.getProducts().forEach { product ->
                    initDatabaseViewModel.insertProducts(product)
                }
                initDatabaseViewModel.userUuid.removeObserver { }
            }
        }
    }
}
