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
import androidx.navigation.fragment.navArgs
import com.example.navsample.ApplicationContext
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
                signInViewModel.onSignInClick(email, password)
            }
        } else if (navArgs.signUp) {
            binding.confirmPasswordLayout.visibility = View.VISIBLE
            actionOnClick = { email, password ->
                signInViewModel.onSignUpClick(email, password)
            }
        }

        binding.confirmButton.setOnClickListener { _ ->
            val email = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()
            actionOnClick?.let { it(email, password) }
            if (signInViewModel.isLogged()) {
                val userId = signInViewModel.getUserId()
                setUserIdToPreferences(userId)
                FirestoreHelperSingleton.initialize(userId)
                syncDatabaseViewModel.loadNotAddedList()
                Navigation.findNavController(requireView()).popBackStack()
            } else {
                Toast.makeText(requireContext(), "SOMETHING WENT WRONG", Toast.LENGTH_SHORT).show()
            }
        }
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
}
