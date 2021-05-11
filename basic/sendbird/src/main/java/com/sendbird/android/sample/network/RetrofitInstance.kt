package com.sendbird.android.sample.network

import com.sendbird.android.sample.main.BaseApplication
import com.sendbird.android.sample.main.BaseApplication.MASTER_TOKEN
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitInstance {

    private val baseUrl = "https://api-${BaseApplication.APP_ID}.sendbird.com"

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
                                .header("Api-Token", MASTER_TOKEN)
                                .method(original.method, original.body)
                                .build()

                        return chain.proceed(request)
                    }
                }).build()

        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

    }
}