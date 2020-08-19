package com.example.accelerometer.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.accelerometer.viewModel.MainActivityViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelsFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  when {
            modelClass.isAssignableFrom(MainActivityViewModel::class.java) -> MainActivityViewModel() as T
            else -> throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}