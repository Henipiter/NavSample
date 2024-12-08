package com.example.navsample.viewmodels.auth


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navsample.auth.AccountServiceImpl
import com.example.navsample.auth.User
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {
    private val accountService = AccountServiceImpl()
    val loggingFinish = MutableLiveData(false) //TODO create listener
    val currentUser = MutableLiveData<User>()
    var errorMessage: String? = null


    fun onSignInClick(email: String, password: String) {
        viewModelScope.launch {
            accountService.signIn(email, password) { message ->
                errorMessage = message
                loggingFinish.postValue(true)
            }
        }
    }

    fun onSignUpClick(email: String, password: String) {
        viewModelScope.launch {
            accountService.signUp(email, password) { message ->
                errorMessage = message
                loggingFinish.postValue(true)
            }
        }
    }

    fun onSignOutClick() {
        viewModelScope.launch {
            accountService.signOut()
        }
    }

    fun isLogged(): Boolean {
        return accountService.currentUserId.isNotEmpty()
    }

    fun getUserId(): String {
        return accountService.currentUserId
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            accountService.currentUser.collect {
                currentUser.postValue(it)
            }
        }
    }
}