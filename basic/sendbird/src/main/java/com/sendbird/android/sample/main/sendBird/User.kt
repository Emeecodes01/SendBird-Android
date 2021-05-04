package com.sendbird.android.sample.main.sendBird

import android.util.Log
import com.sendbird.android.sample.network.NetworkRequest
import com.sendbird.android.sample.network.createUser.CreateUserRequest
import com.sendbird.android.sample.network.createUser.CreateUserResponse

class User {

    fun createUser(userData: CreateUserRequest, createUserResponse: (CreateUserResponse) -> Unit) {
       val  networkRequest = NetworkRequest()
        networkRequest.createUser(userData, {
            createUserResponse(it)
        }, {
            Log.d("okh", it)
        })
    }

}