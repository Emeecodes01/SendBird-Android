package com.sendbird.android.sample.network

import com.sendbird.android.sample.network.createUser.CreateUserRequest
import com.sendbird.android.sample.network.createUser.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {

    @POST("/v3/users/")
    fun createUser(@Body user: CreateUserRequest): Call<UserResponse>

}