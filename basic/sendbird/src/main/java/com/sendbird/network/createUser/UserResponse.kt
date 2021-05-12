package com.sendbird.network.createUser

import com.sendbird.android.shadow.com.google.gson.annotations.SerializedName

data class UserResponse(
        @SerializedName("user_id") val user_id: String,
        @SerializedName("nickname") val nickname: String,
        @SerializedName("profile_url") val profile_url: String,
        @SerializedName("access_token") val access_token: String
)
