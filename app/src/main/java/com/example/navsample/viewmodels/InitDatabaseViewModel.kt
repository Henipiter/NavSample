package com.example.navsample.viewmodels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navsample.ApplicationContext
import com.example.navsample.entities.Category
import com.example.navsample.entities.FirebaseHelper
import com.example.navsample.entities.FirebaseHelperFactory
import com.example.navsample.entities.Product
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.Store
import java.util.UUID

class InitDatabaseViewModel : ViewModel() {

    private var firebaseHelper: FirebaseHelper
    private var roomDatabaseHelper: RoomDatabaseHelper

    var imageUuid = MutableLiveData<String>()
    lateinit var store: MutableLiveData<Store>
    lateinit var receipt: MutableLiveData<Receipt>
    lateinit var product: MutableLiveData<ArrayList<Product>>

    lateinit var category: MutableLiveData<Category?>

    init {
        val myPref = ApplicationContext.context?.getSharedPreferences(
            "preferences", AppCompatActivity.MODE_PRIVATE
        )
        if (myPref?.getString("userId", "") == "") {
            myPref.edit().putString("userId", UUID.randomUUID().toString()).apply()
        }
        val userUuid = myPref?.getString("userId", null) ?: throw Exception("NOT SET userId")
        firebaseHelper = FirebaseHelperFactory.build(userUuid)

        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
        roomDatabaseHelper = RoomDatabaseHelper(dao!!)
    }

}
