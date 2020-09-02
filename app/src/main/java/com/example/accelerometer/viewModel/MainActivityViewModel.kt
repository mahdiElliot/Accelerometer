package com.example.accelerometer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    val accRTxt = MutableLiveData<Int>().apply { value = 50 }
    val accCenterFreq = MutableLiveData<Double>().apply { value = 0.5 }
    val accWidthFreq = MutableLiveData<Double>().apply { value = 100.0 }
    val accOrder = MutableLiveData<Int>().apply { value = 2 }
    val accSaveTime = MutableLiveData<Int>().apply { value = 5 }
    val accSubmitSetting = MutableLiveData<Boolean>().apply { value = false }
    val gyroRTxt = MutableLiveData<Int>().apply { value = 50 }
    val gyroCenterFreq = MutableLiveData<Double>().apply { value = 0.5 }
    val gyroWidthFreq = MutableLiveData<Double>().apply { value = 100.0 }
    val gyroOrder = MutableLiveData<Int>().apply { value = 2 }
    val gyroSaveTime = MutableLiveData<Int>().apply { value = 5 }
    val isOnTimer = MutableLiveData<Boolean>().apply { value = false }
    val timeNow = MutableLiveData<Long>()
    val timeMilli = MutableLiveData<Long>()
    val gyroSubmitSetting = MutableLiveData<Boolean>().apply { value = false }
}