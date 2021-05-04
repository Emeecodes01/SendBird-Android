package com.sendbird.android.sample.network

import com.sendbird.android.sample.network.createUser.CreateUserRequest
import com.sendbird.android.sample.network.createUser.CreateUserResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class NetworkRequest {

    lateinit var api: Api

    fun createUser(userData: CreateUserRequest, createUserResponse: (CreateUserResponse) -> Unit, error: (String) -> Unit) {

        api = RetrofitInstance().getClient().create(Api::class.java)

        api.createUser(userData).enqueue(object : Callback<CreateUserResponse> {
            override fun onResponse(call: Call<CreateUserResponse>, response: Response<CreateUserResponse>) {

                if (response.isSuccessful) {
                    response.body()?.let { createUserResponse(it) }
                }else{
                    response.errorBody()?.let { error(it.string()) }
                }
            }

            override fun onFailure(call: Call<CreateUserResponse>, t: Throwable) {
                t.message?.let { error(it) }
            }

        })
    }
}