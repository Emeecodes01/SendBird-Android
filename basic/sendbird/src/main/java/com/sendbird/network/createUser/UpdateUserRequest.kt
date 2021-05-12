package com.sendbird.network.createUser

import com.sendbird.android.shadow.com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
        @SerializedName("user_id") val user_id: String,
        @SerializedName("issue_access_token") val issue_access_token: Boolean = true
)
