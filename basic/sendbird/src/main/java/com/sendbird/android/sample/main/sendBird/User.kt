package com.sendbird.android.sample.main.sendBird

import com.google.gson.Gson
import com.sendbird.android.sample.network.NetworkRequest
import com.sendbird.android.sample.network.createUser.CreateUserRequest
import com.sendbird.android.sample.network.createUser.UpdateUserRequest
import com.sendbird.android.sample.network.createUser.UserResponse

class User {

    val gson = Gson()

    fun createUser(userData: CreateUserRequest, accessToken: String?, userResponse: (UserResponse) -> Unit, error: (ErrorData) -> Unit) {
        val networkRequest = NetworkRequest()

        if (accessToken.isNullOrEmpty()) {

            networkRequest.createUser(userData, {
                userResponse(it)
            }, {

                networkRequest.updateUser(UpdateUserRequest(userData.user_id, true), {
                    userResponse(it)

                    val loginData = UserData(userData.user_id, userData.nickname, it.access_token)

                    Connect().login(loginData) { user, loginError ->

                        user?.let {
                            userResponse(UserResponse(it.userId, it.nickname, it.profileUrl, ""))
                        } ?: kotlin.run {

                        }
                    }
                }, {

                })

            })

        } else {

            val loginData = UserData(userData.user_id, userData.nickname, accessToken)

            Connect().login(loginData) { user, loginError ->

                user?.let {
                    userResponse(UserResponse(it.userId, it.nickname, it.profileUrl, ""))
                } ?: kotlin.run {

                }
            }
        }
    }

}