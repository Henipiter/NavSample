package com.example.navsample.viewmodels.auth


import androidx.lifecycle.MutableLiveData
import com.example.navsample.auth.AccountServiceImpl
import com.example.navsample.auth.User

class SignInViewModel(
) : NotesAppViewModel() {
    private val accountService = AccountServiceImpl()
    val currentUserId = MutableLiveData("") //TODO create listener
    val currentUser = MutableLiveData<User>()

    fun onSignInClick(email: String, password: String) {
        launchCatching {
            accountService.signIn(email, password)
            currentUserId.postValue(accountService.currentUserId)
        }
    }

    fun onSignUpClick(email: String, password: String) {
        launchCatching {
            accountService.signUp(email, password)
            currentUserId.postValue(accountService.currentUserId)
        }
    }

    fun onSignOutClick() {
        launchCatching {
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
        launchCatching {
            accountService.currentUser.collect {
                currentUser.postValue(it)
            }
        }
    }
}