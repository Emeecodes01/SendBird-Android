package com.sendbird.main.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserData(val id: String, val nickname: String, var accessToken: String = "") : Parcelable
