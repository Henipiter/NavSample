package com.example.navsample.fragments.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.navsample.ApplicationContext
import com.example.navsample.R
import com.example.navsample.databinding.FragmentSigningPanelBinding
import com.example.navsample.entities.FirestoreHelperSingleton
import com.example.navsample.viewmodels.SyncDatabaseViewModel
import com.example.navsample.viewmodels.auth.SignInViewModel


class SigningPanelFragment : Fragment() {
    private var _binding: FragmentSigningPanelBinding? = null
    private val signInViewModel: SignInViewModel by activityViewModels()
    private val syncDatabaseViewModel: SyncDatabaseViewModel by activityViewModels()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSigningPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButtons()
        signInViewModel.getCurrentUser()
        initObserver()


        binding.signInButton.setOnClickListener {
            val action = SigningPanelFragmentDirections.actionSigningPanelFragmentToSignInFragment(
                signIn = true, signUp = false
            )
            Navigation.findNavController(requireView()).navigate(action)
        }
        binding.signUpButton.setOnClickListener {
            val action = SigningPanelFragmentDirections.actionSigningPanelFragmentToSignInFragment(
                signIn = false, signUp = true
            )
            Navigation.findNavController(requireView()).navigate(action)
        }
        binding.skipButton.setOnClickListener {
            if (shouldInitLogin()) {
                initLogInMarkAsDone()
                changeStartDestination()
            }
            Navigation.findNavController(requireView()).popBackStack()
        }
        binding.signOutButton.setOnClickListener {
            signInViewModel.onSignOutClick()

            if (!signInViewModel.isLogged()) {
                Log.d("USER_ID", "Clear user id")
                clearUserIdInPreferences()
                FirestoreHelperSingleton.initialize("")
                clearDatabase()
                initButtons()
            } else {
                Log.d("USER_ID", "User is still logged")
            }
        }
    }

    private fun clearDatabase() {
        syncDatabaseViewModel.deleteAllData()
    }

    private fun initObserver() {
        signInViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.usernameInfo.text = ""
            } else {
                binding.usernameInfo.text = it.email
            }
        }
    }

    private fun initButtons() {

        if (signInViewModel.isLogged()) {
            binding.signInButton.visibility = View.GONE
            binding.signUpButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE
            binding.deleteAccountButton.visibility = View.VISIBLE
            binding.skipButton.visibility = View.GONE
        } else {
            binding.signInButton.visibility = View.VISIBLE
            binding.signUpButton.visibility = View.VISIBLE
            binding.signOutButton.visibility = View.GONE
            binding.deleteAccountButton.visibility = View.GONE
            if (shouldInitLogin()) {
                binding.skipButton.visibility = View.VISIBLE
            } else {
                binding.skipButton.visibility = View.GONE
            }
        }
    }

    private fun clearUserIdInPreferences() {
        val myPref = ApplicationContext.context?.getSharedPreferences(
            "preferences", AppCompatActivity.MODE_PRIVATE
        )
        myPref?.edit()?.putString("userId", "")?.apply()
    }

    private fun shouldInitLogin(): Boolean {
        return ApplicationContext.context
            ?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
            ?.getBoolean("shouldInitLogin", true)
            ?: true
    }

    private fun changeStartDestination() {
        val navGraph = findNavController().navInflater.inflate(R.navigation.main_nav)
        navGraph.setStartDestination(R.id.listingFragment)
        findNavController().graph = navGraph
    }

    private fun initLogInMarkAsDone() {
        val myPref = ApplicationContext.context
            ?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)
        myPref?.edit()?.putBoolean("shouldInitLogin", false)?.apply()
    }

}