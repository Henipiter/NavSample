package com.example.navsample.fragments.saving

import androidx.fragment.app.Fragment

abstract class AddingFragment : Fragment() {
    abstract fun defineToolbar()
    abstract fun save()
    abstract fun initObserver()
    abstract fun consumeNavArgs()
    abstract fun clearInputs()
}