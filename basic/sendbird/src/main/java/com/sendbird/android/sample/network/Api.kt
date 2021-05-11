package com.sendbird.android.sample.network

import com.sendbird.android.sample.network.createUser.ConnectUserRequest
import com.sendbird.android.sample.network.createUser.UpdateUserRequest
import com.sendbird.android.sample.network.createUser.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface Api {

    @POST("/v3/users/")
    fun createUser(@Body user: ConnectUserRequest): Call<UserResponse>

    @PUT("/v3/users/{user_id}")
    fun updateUser(@Path("user_id") user_id : String, @Body user: UpdateUserRequest): Call<UserResponse>

}