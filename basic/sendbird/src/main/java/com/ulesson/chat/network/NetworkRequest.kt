package com.ulesson.chat.network

import android.util.Log
import com.ulesson.chat.network.userModel.ConnectUserRequest
import com.ulesson.chat.network.userModel.UpdateUserRequest
import com.ulesson.chat.network.userModel.UserResponse
import com.ulesson.chat.utils.PreferenceUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkRequest {

    private var api = RetrofitInstance().getClient().create(Api::class.java)

    fun createUser(
        userData: ConnectUserRequest,
        createUserResponse: (UserResponse) -> Unit,
        error: (String) -> Unit
    ) {

        api.createUser(userData).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {

                if (response.isSuccessful) {
                    response.body()?.let { createUserResponse(it) }
                } else {
                    response.errorBody()?.let { error(it.string()) }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                t.message?.let { error(it) }
            }

        })
    }

    fun updateUser(
        userData: UpdateUserRequest,
        createUserResponse: (UserResponse) -> Unit,
        error: (String) -> Unit
    ) {

        Log.d("okh", PreferenceUtils.getMasterToken() +"master token")

        api.updateUser(userData.user_id, userData).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {

                if (response.isSuccessful) {
                    response.body()?.let { createUserResponse(it) }
                } else {
                    response.errorBody()?.let { error(it.string()) }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                t.message?.let { error(it) }
            }

        })
    }

}