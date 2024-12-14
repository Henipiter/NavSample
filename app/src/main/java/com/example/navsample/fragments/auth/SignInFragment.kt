package com.example.navsample.fragments.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.navsample.ApplicationContext
import com.example.navsample.R
import com.example.navsample.databinding.FragmentSignInBinding
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.viewmodels.SyncDatabaseViewModel
import com.example.navsample.viewmodels.auth.SignInViewModel


class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val signInViewModel: SignInViewModel by activityViewModels()
    private val syncDatabaseViewModel: SyncDatabaseViewModel by activityViewModels()
    private val navArgs: SignInFragmentArgs by navArgs()
    private var actionOnClick: ((String, String) -> Unit)? = null
    private var validationMessage = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (navArgs.signIn) {
            binding.confirmPasswordLayout.visibility = View.INVISIBLE
            actionOnClick = { email, password ->
                validationMessage = validateSignIn()
                if (validationMessage.isEmpty()) {
                    signInViewModel.onSignInClick(
                        email,
                        password,
                        { onLoginSuccess() },
                        { onLoginFailure() }
                    )
                } else {
                    Toast.makeText(requireContext(), validationMessage, Toast.LENGTH_SHORT).show()
                }
            }
        } else if (navArgs.signUp) {
            binding.confirmPasswordLayout.visibility = View.VISIBLE
            actionOnClick = { email, password ->
                validationMessage = validateSignUp()
                if (validationMessage.isEmpty()) {
                    signInViewModel.onSignUpClick(
                        email,
                        password,
                        { onLoginSuccess() },
                        { onLoginFailure() })
                } else {
                    Toast.makeText(requireContext(), validationMessage, Toast.LENGTH_SHORT).show()
                }

            }
        }

        binding.confirmButton.setOnClickListener { _ ->
            val email = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString()
            actionOnClick?.let { it(email, password) }

        }
    }

    private fun validateSignUp(): String {
        if (!validateEmailInput()) {
            return "Bad email"
        } else if (!validatePasswordInput()) {
            return "Password have to be longer than 6 signs"
        } else if (!validatePasswordConfirmationInput()) {
            return "Passwords are different"
        }
        return ""
    }

    private fun validateSignIn(): String {
        if (!validateEmailInput()) {
            return "Bad email"
        } else if (!validatePasswordInput()) {
            return "Password have to be longer than 6 signs"
        }
        return ""
    }

    private fun onLoginFailure() {
        Log.d("Firestore1", "Error ${signInViewModel.errorMessage}")
        Toast.makeText(requireContext(), "Auth error", Toast.LENGTH_SHORT).show()

    }

    private fun onLoginSuccess() {
        if (shouldInitLogin()) {
            initLogInMarkAsDone()
            changeStartDestination()
        }
        val userId = signInViewModel.getUserId()
        setUserIdToPreferences(userId)
        FirestoreHelperSingleton.initialize(userId)
        syncDatabaseViewModel.setFirebaseHelper()
        syncDatabaseViewModel.loadNotAddedList()
        Toast.makeText(requireContext(), "Auth success", Toast.LENGTH_SHORT).show()
        Navigation.findNavController(requireView()).popBackStack()
    }

    private fun setUserIdToPreferences(id: String) {
        Log.d("USER_ID", "Try setting if: $id")
        val myPref = ApplicationContext.context?.getSharedPreferences(
            "preferences", AppCompatActivity.MODE_PRIVATE
        )
        val currentUserId = myPref?.getString("userId", "")
        if (currentUserId == "") {
            myPref.edit().putString("userId", id).apply()
            Log.d("USER_ID", "User id acquired: $id")
        } else {
            Log.d("USER_ID", "User id is currently set: $currentUserId")
        }
    }

    private fun validateEmailInput(): Boolean {
        val email = binding.usernameInput.text.toString().trim()
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
    }

    private fun validatePasswordInput(): Boolean {
        val password = binding.passwordInput.text
        return password.length >= 6
    }

    private fun validatePasswordConfirmationInput(): Boolean {
        val password = binding.passwordInput.text
        val confirmPassword = binding.confirmPasswordInput.text
        return password == confirmPassword
    }

    private fun changeStartDestination() {
        val navGraph = findNavController().navInflater.inflate(R.navigation.main_nav)
        navGraph.setStartDestination(R.id.listingFragment)
        findNavController().graph = navGraph
    }

    private fun shouldInitLogin(): Boolean {
        return ApplicationContext.context
            ?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
            ?.getBoolean("shouldInitLogin", true)
            ?: true
    }

    private fun initLogInMarkAsDone() {
        val myPref = ApplicationContext.context
            ?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
        myPref?.edit()?.putBoolean("shouldInitLogin", false)?.apply()
    }
}

