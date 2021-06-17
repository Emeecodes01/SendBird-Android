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




    fun endChat(questionId: Int, endTime: String, channelUrl: String, success: () -> Unit, error: (String?) -> Unit) {
        val req = hashMapOf (
            "session_end" to endTime,
            "channel_url" to channelUrl
        )

        api = UlessonRetrofitInstance().getClient().create(Api::class.java)

        api.endChat(questionId, req).enqueue(object: Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {

                if (response.isSuccessful) {
                    success.invoke()
                }else {
                    error.invoke(response.errorBody()?.string())
                }

            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                t.printStackTrace()
                error.invoke(t.message)
            }
        })

    }


    /**
     * Ensure you run this on a background thread
     */
    fun endChat(questionId: Int, endTime: String, channelUrl: String): Response<Any> {
        val req = hashMapOf (
            "session_end" to endTime,
            "channel_url" to channelUrl
        )

        api = UlessonRetrofitInstance().getClient().create(Api::class.java)

        return api.endChat(questionId,req).execute()
    }

}