package com.example.unifocus.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unifocus.data.repository.UniFocusRepository

class UniFocusViewModelFactory(
    private val repository: UniFocusRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UniFocusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UniFocusViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}