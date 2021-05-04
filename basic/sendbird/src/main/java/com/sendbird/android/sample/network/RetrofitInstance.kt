package com.sendbird.android.sample.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitInstance {

    val appId = "58BB27E6-E4EA-4448-83F5-8E03A873AD6A"
    val baseUrl = "https://api-${appId}.sendbird.com"

    fun getClient(): Retrofit {

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {

                        val original = chain.request()

                        val request = original.newBuilder()
                                .header("Content-Type", "application/json; charset=utf8")
                                .header("Api-Token", "523ebc6218ee5284fd4743b2ef5a4f96dbd2f924")
                                .method(original.method, original.body)
                                .build()

                        return chain.proceed(request)
                    }
                }).build()

        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit

    }
}