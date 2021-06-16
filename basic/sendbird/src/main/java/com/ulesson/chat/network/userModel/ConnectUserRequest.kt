package com.ulesson.chat.network.userModel

import com.sendbird.android.shadow.com.google.gson.annotations.SerializedName

data class ConnectUserRequest(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profile_url") val profile_url: String,
    @SerializedName("issue_access_token") val issue_access_token: Boolean = true
)
