package com.sendbird.android.sample.network

import com.sendbird.android.sample.network.createUser.ConnectUserRequest
import com.sendbird.android.sample.network.createUser.UpdateUserRequest
import com.sendbird.android.sample.network.createUser.UserResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class NetworkRequest {

    private var api = RetrofitInstance().getClient().create(Api::class.java)

    fun createUser(userData: ConnectUserRequest, createUserResponse: (UserResponse) -> Unit, error: (String) -> Unit) {

        api.createUser(userData).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {

                if (response.isSuccessful) {
                    response.body()?.let { createUserResponse(it) }
                }else{
                    response.errorBody()?.let { error(it.string()) }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                t.message?.let { error(it) }
            }

        })
    }

    fun updateUser(userData: UpdateUserRequest, createUserResponse: (UserResponse) -> Unit, error: (String) -> Unit) {

        api.updateUser(userData.user_id, userData).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {

                if (response.isSuccessful) {
                    response.body()?.let { createUserResponse(it) }
                }else{
                    response.errorBody()?.let { error(it.string()) }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                t.message?.let { error(it) }
            }

        })
    }

}