package com.sendbird.main.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserData(val id: String, val nickname: String, val accessToken: String = "") : Parcelable
