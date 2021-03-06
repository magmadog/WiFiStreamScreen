package com.sarbaevartur.wifistreamscreen.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrafficPoint(
    val time: Long,
    val bytes: Long
) : Parcelable