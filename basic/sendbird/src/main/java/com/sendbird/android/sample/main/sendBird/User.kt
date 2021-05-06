package com.sendbird.android.sample.main.sendBird

import com.google.gson.Gson
import com.sendbird.android.sample.network.NetworkRequest
import com.sendbird.android.sample.network.createUser.CreateUserRequest
import com.sendbird.android.sample.network.createUser.UserResponse

class User {

    val gson = Gson()

    fun createUser(userData: CreateUserRequest, accessToken: String?, userResponse: (UserResponse) -> Unit, error: (ErrorData) -> Unit) {
        val networkRequest = NetworkRequest()

        if (accessToken == null) {

            networkRequest.createUser(userData, {
                userResponse(it)
            }, {
                val errorData = Gson().fromJson(gson.toJson(it), ErrorData::class.java)
                error(errorData)
            })

        } else {

            val loginData = UserData(userData.user_id, userData.nickname, accessToken)

            Connect().login(loginData) { user, loginError ->

                user?.let {
                    userResponse(UserResponse(it.userId, it.nickname, it.profileUrl, ""))
                } ?: kotlin.run {
                    error(loginError)
                }
            }
        }
    }

}