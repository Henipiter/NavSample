package com.example.navsample.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navsample.R
import com.example.navsample.adapters.TableCountListAdapter
import com.example.navsample.databinding.FragmentExportDataBinding
import com.example.navsample.entities.relations.TableCounts
import com.example.navsample.viewmodels.ExportDataViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter


class ExportDataFragment : Fragment() {
    private var _binding: FragmentExportDataBinding? = null

    private val binding get() = _binding!!
    private val exportDataViewModel: ExportDataViewModel by activityViewModels()

    private lateinit var recycleViewEvent: RecyclerView
    private lateinit var listAdapter: TableCountListAdapter
    private var recycleList = arrayListOf<TableCounts>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.top_menu_export)
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.title = "Export data"

        exportDataViewModel.getTableCounts()
        exportDataViewModel.getAllData()

        recycleList = exportDataViewModel.tableCounts.value ?: arrayListOf()
        recycleViewEvent = binding.recycleView
        listAdapter = TableCountListAdapter(recycleList)
        recycleViewEvent.adapter = listAdapter
        recycleViewEvent.layoutManager = GridLayoutManager(requireContext(), 2)
        initObserver()

        binding.toolbar.setNavigationOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.export -> {
                    if (isDataExist()) {
                        Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    saveCsvFile(prepareFileContents())
                    true
                }

                else -> false
            }
        }
    }

    private fun isDataExist(): Boolean {
        return exportDataViewModel.allData.value?.isEmpty() ?: false
    }

    private fun prepareFileContents(): String {
        val data = StringBuilder(
            "storeName;storeNip;receiptPln;receiptPtu;receiptDate;receiptTime;" +
                    "productName;productQuantity;productUnitPrice;productSubtotalPrice;productDiscount;" +
                    "productFinalPrice;productPtuType;productRaw;productValidPrice;categoryName;categoryColor\n"
        )

        exportDataViewModel.allData.value?.forEach { products ->
            data.append(products.storeName).append(";")
            data.append(products.storeNip).append(";")
            data.append(products.receiptPln).append(";")
            data.append(products.receiptPtu).append(";")
            data.append(products.receiptDate).append(";")
            data.append(products.receiptTime).append(";")
            data.append(products.productName).append(";")
            data.append(products.productQuantity).append(";")
            data.append(products.productUnitPrice).append(";")
            data.append(products.productSubtotalPrice).append(";")
            data.append(products.productDiscount).append(";")
            data.append(products.productFinalPrice).append(";")
            data.append(products.productPtuType).append(";")
            data.append(products.productRaw).append(";")
            data.append(products.productValidPrice).append(";")
            data.append(products.categoryName).append(";")
            data.append(products.categoryColor).append("\n")
        }
        return data.toString()
    }

    private fun saveCsvFile(data: String) {


        val fileName = "receipts45.csv"
        var fileOutputStream: FileOutputStream? = null
        var outputStreamWriter: OutputStreamWriter? = null

        try {
            val externalDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outputFile = File(externalDir, fileName)

            fileOutputStream = FileOutputStream(outputFile)
            outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.write(data)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "text/csv")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            requireContext().contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )
            Toast.makeText(requireContext(), "SUCCESS", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        } finally {
            try {
                outputStreamWriter?.close()
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace();
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        exportDataViewModel.tableCounts.observe(viewLifecycleOwner) {
            it?.let {
                listAdapter.recycleList = it
                listAdapter.notifyDataSetChanged()
            }
        }
    }
}