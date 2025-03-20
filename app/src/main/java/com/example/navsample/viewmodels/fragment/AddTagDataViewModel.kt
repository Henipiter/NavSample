package com.example.navsample.viewmodels.fragment

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.navsample.R
import com.example.navsample.dto.inputmode.AddingInputType
import com.example.navsample.entities.ReceiptDatabase
import com.example.navsample.entities.RoomDatabaseHelper
import com.example.navsample.entities.database.Tag
import com.example.navsample.entities.inputs.TagErrorInputsMessage
import com.example.navsample.entities.inputs.TagInputs
import kotlinx.coroutines.launch

class AddTagDataViewModel(
    private var application: Application
) : AndroidViewModel(application) {

    private var roomDatabaseHelper: RoomDatabaseHelper

    var inputType = AddingInputType.EMPTY.name
    var tagId = ""

    var tagList = MutableLiveData<ArrayList<Tag>>()
    var tagById = MutableLiveData<Tag?>()
    var savedTag = MutableLiveData<Tag>()

    init {
        val dao = ReceiptDatabase.getInstance(application).receiptDao
        roomDatabaseHelper = RoomDatabaseHelper(dao)
    }

    fun refreshTagList() {
        viewModelScope.launch {
            tagList.postValue(roomDatabaseHelper.getAllTags() as ArrayList<Tag>)
        }
    }

    fun deleteTag(tagId: String, onFinish: () -> Unit) {
        viewModelScope.launch {
            roomDatabaseHelper.deleteTagProductTag(tagId)
            roomDatabaseHelper.deleteTag(tagId)
            onFinish.invoke()
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
        }
    }

    fun updateTag(newTag: Tag) {
        viewModelScope.launch {
            val updatedTag = roomDatabaseHelper.updateTag(newTag)
            savedTag.postValue(updatedTag)
        }
    }

    fun validateObligatoryFields(tagInputs: TagInputs): TagErrorInputsMessage {
        val errors = TagErrorInputsMessage()
        errors.name = validateName(tagInputs.name)
        return errors

    }

    private fun validateName(text: CharSequence?): String? {
        val currentTagName = tagById.value?.name
        val tagList = tagList.value

        return if (text.isNullOrEmpty()) {
            ContextCompat.getString(application, R.string.empty_value_error)
        } else if (text.toString() == currentTagName) {
            null
        } else if (tagList?.find { it.name == text.toString() } != null) {
            ContextCompat.getString(application, R.string.tag_already_exists)
        } else {
            null
        }
    }

}
