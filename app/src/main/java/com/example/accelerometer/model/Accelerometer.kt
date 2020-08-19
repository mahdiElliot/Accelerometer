package com.example.accelerometer.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Accelerometer(
    val x: Double,
    val y: Double,
    val z: Double
):Parcelable