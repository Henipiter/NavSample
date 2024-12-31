package com.example.navsample.viewmodels.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.ApplicationContext
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Tag
import kotlinx.coroutines.launch

class AddTagDataViewModel : ViewModel() {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var tagId = ""

    var tagList = MutableLiveData<ArrayList<Tag>>()
    var tagById = MutableLiveData<Tag?>()
    var savedTag = MutableLiveData<Tag>()

    init {

        val dao = ApplicationContext.context?.let { ReceiptDatabase.getInstance(it).receiptDao }
            ?: throw Exception("NOT SET DATABASE")
        roomDatabaseHelper = RoomDatabaseHelper(dao)
    }

    fun refreshTagList() {
        viewModelScope.launch {
            tagList.postValue(roomDatabaseHelper.getAllTags() as ArrayList<Tag>)
        }
    }

    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            val deletedTag = roomDatabaseHelper.deleteTag(tagId)
            FirestoreHelperSingleton.getInstance().delete(deletedTag) { id ->
                viewModelScope.launch { roomDatabaseHelper.markTagAsDeleted(id) }
            }
        }
    }

    fun getTagById(id: String) {
        viewModelScope.launch {
            tagById.postValue(roomDatabaseHelper.getTagById(id))
        }
    }

    fun insertTag(newTag: Tag, generateId: Boolean = true) {
        viewModelScope.launch {
            val insertedTag = roomDatabaseHelper.insertTag(newTag, generateId)
            savedTag.postValue(insertedTag)
            FirestoreHelperSingleton.getInstance().addFirestore(insertedTag) {
                viewModelScope.launch {
                    roomDatabaseHelper.updateTagFirestoreId(insertedTag.id, it)
                }
            }
        }
    }

    fun updateTag(newTag: Tag) {
        viewModelScope.launch {
            val updatedTag = roomDatabaseHelper.updateTag(newTag)
            savedTag.postValue(updatedTag)
            if (newTag.firestoreId.isNotEmpty()) {
                FirestoreHelperSingleton.getInstance().updateFirestore(updatedTag) {
                    viewModelScope.launch { roomDatabaseHelper.markTagAsUpdated(updatedTag.id) }
                }
            }
        }
    }


}
