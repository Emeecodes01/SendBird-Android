package com.ulesson.chat.main.model

import android.os.Parcelable
import com.sendbird.android.GroupChannelParams
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class UserGroup(val channelUrl: String, val groupChannelParams: @RawValue GroupChannelParams) :
    Parcelable
