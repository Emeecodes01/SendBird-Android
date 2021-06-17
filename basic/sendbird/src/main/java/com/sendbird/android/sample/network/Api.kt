package com.sendbird.android.sample.network

import com.sendbird.android.sample.network.createUser.CreateUserRequest
import com.sendbird.android.sample.network.createUser.CreateUserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface Api {

    @POST("/v3/users/")
    fun createUser(@Body user: CreateUserRequest): Call<CreateUserResponse>

    @POST("/api/v1/questions/{questionId}/chat/end")
    fun endChat(@Path("questionId") questionId: Int, @Body request: HashMap<String, String>): Call<Any>

}