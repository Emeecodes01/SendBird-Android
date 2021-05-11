package com.sendbird.android.sample.main.sendBird

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ErrorData(val message: String?, val code: Int, val error: Boolean) : Parcelable
