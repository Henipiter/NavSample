package com.example.navsample.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class TestDatabaseViewModel : ViewModel() {

    private val firestore = Firebase.firestore

    var productListHeader = ""
    var storeListHeader = ""
    var categoryListHeader = ""
    var receiptListHeader = ""
    val productList = ArrayList<String>()
    val storeList = ArrayList<String>()
    val categoryList = ArrayList<String>()
    val receiptList = ArrayList<String>()

    fun getAllUsers() {
        val userIdList = listOf(
            "0c1c1ba9-13a1-4d9c-bf03-d280b69f4519",
            "16c51ca3-d5fc-4f62-b44c-bc6ecd2a69d6",
            "4584d3a0-e2a5-4dad-bd98-6f4a50bf5abb",
            "45bf29f2-dac5-4a6a-a8d1-a0a24c81498e",
            "522b8eef-b861-419b-b176-4b82590c6eda",
            "5d9ac061-e3bb-4c19-a546-c1b2d0398f64",
            "62ebb56a-3667-44ec-9a18-273d9c505f6e",
            "6d874ba3-2605-4c2e-94b5-9806fd2ee42f",
            "8e97b702-dbc0-4f33-b790-b40b005349b3",
            "b2775fa2-6d17-416a-bc50-d5762ba05f9f",
            "cc66268b-7221-4851-85a8-82e9f75ed1b9",
            "db1afecd-65ae-41b8-9ae3-62b4e4750a42",
            "dc44958f-9c09-458f-95cf-d1585e81a6b2",
            "deda3a34-40ae-41f6-a9a3-9fb5b0eade07",
            "eedfe8f4-a5ef-486b-bcca-e0ed297c8949",
            "f1e5e2dd-0d39-4a2e-b60d-1dd62ab76d9f",
            "null"
        )
        for (id in userIdList) {
            getUsersProducts(id)
            getUsersStores(id)
            getUsersReceipts(id)
            getUsersCategory(id)
        }
    }

    private fun getUsersProducts(userId: String) {
        firestore.collection("user").document(userId)
            .collection("products").get()
            .addOnSuccessListener { result ->
                Log.w("FirestoreData", "getUsersProducts")
                if (result.isEmpty) {
                    Log.w("FirestoreData", "EMPTY")

                }
                for (document in result) {
                    productListHeader =
                        "userId,id,receiptId,name,categoryId,quantity,unitPrice,subtotalPrice,discount,finalPrice,ptuType,raw,validPrice"
                    productList.add(
                        "${userId}," +
                                "${document.data["id"]}," +
                                "${document.data["receiptId"]}," +
                                "${document.data["name"].toString().replace(",", ".")}," +
                                "${document.data["categoryId"]}," +
                                "${document.data["quantity"]}," +
                                "${document.data["unitPrice"]}," +
                                "${document.data["subtotalPrice"]}," +
                                "${document.data["discount"]}," +
                                "${document.data["finalPrice"]}," +
                                "${document.data["ptuType"]}," +
                                "${document.data["raw"].toString().replace(",", ".")}," +
                                "${document.data["validPrice"]}"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreData", "getUsersProducts")
                Log.w("FirestoreData", "Error getting comments: ", exception)
            }
    }

    private fun getUsersStores(userId: String) {
        firestore.collection("user").document(userId)
            .collection("stores").get()
            .addOnSuccessListener { result ->
                Log.w("FirestoreData", "getUsersProducts")
                if (result.isEmpty) {
                    Log.w("FirestoreData", "EMPTY")
                }
                for (document in result) {
                    storeListHeader =
                        "userId,id,name,nip,defaultCategoryId"
                    storeList.add(
                        "${userId}," +
                                "${document.data["id"]}," +
                                "${document.data["name"]}," +
                                "${document.data["nip"]}," +
                                "${document.data["defaultCategoryId"]}"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreData", "getUsersProducts")
                Log.w("FirestoreData", "Error getting comments: ", exception)
            }
    }

    private fun getUsersReceipts(userId: String) {
        firestore.collection("user").document(userId)
            .collection("receipts").get()
            .addOnSuccessListener { result ->
                Log.w("FirestoreData", "getUsersProducts")
                if (result.isEmpty) {
                    Log.w("FirestoreData", "EMPTY")
                }
                for (document in result) {
                    receiptListHeader =
                        "userId,id,storeId,pln,ptu,date,time,validPrice"
                    receiptList.add(
                        "${userId}," +
                                "${document.data["id"]}," +
                                "${document.data["storeId"]}," +
                                "${document.data["pln"]}," +
                                "${document.data["ptu"]}," +
                                "${document.data["date"]}," +
                                "${document.data["time"]}," +
                                "${document.data["validPrice"]}"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreData", "getUsersProducts")
                Log.w("FirestoreData", "Error getting comments: ", exception)
            }
    }

    private fun getUsersCategory(userId: String) {
        firestore.collection("user").document(userId)
            .collection("categories").get()
            .addOnSuccessListener { result ->
                Log.w("FirestoreData", "getUsersProducts")
                if (result.isEmpty) {
                    Log.w("FirestoreData", "EMPTY")
                }
                for (document in result) {
                    categoryListHeader =
                        "userId,id,name,color"
                    categoryList.add(
                        "${userId}," +
                                "${document.data["id"]}," +
                                "${document.data["name"]}," +
                                "${document.data["color"]}"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreData", "getUsersProducts")
                Log.w("FirestoreData", "Error getting comments: ", exception)
            }
    }


}
