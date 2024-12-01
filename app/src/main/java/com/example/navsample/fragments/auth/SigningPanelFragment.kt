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
import com.example.navsample.ApplicationContext
import com.example.navsample.databinding.FragmentSigningPanelBinding
import com.example.navsample.viewmodels.auth.SignInViewModel


class SigningPanelFragment : Fragment() {
    private var _binding: FragmentSigningPanelBinding? = null
    private val signInViewModel: SignInViewModel by activityViewModels()
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
        binding.signOutButton.setOnClickListener {
            signInViewModel.onSignOutClick()
            if (!signInViewModel.isLogged()) {
                Log.d("USER_ID", "Clear user id")
                clearUserIdInPreferences()
                initButtons()
            } else {
                Log.d("USER_ID", "User is still logged")
            }
        }
    }

    private fun initObserver() {
        signInViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.usernameInfo.text = "Not logged"
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
        } else {
            binding.signInButton.visibility = View.VISIBLE
            binding.signUpButton.visibility = View.VISIBLE
            binding.signOutButton.visibility = View.GONE
            binding.deleteAccountButton.visibility = View.GONE
        }
    }

    private fun clearUserIdInPreferences() {
        val myPref = ApplicationContext.context?.getSharedPreferences(
            "preferences", AppCompatActivity.MODE_PRIVATE
        )
        myPref?.edit()?.putString("userId", "")?.apply()
    }
}