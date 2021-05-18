package com.sendbird.android.sample.network

import com.sendbird.android.sample.utils.PreferenceUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitInstance {

    val appId: String by lazy {
        PreferenceUtils.getSendbirdAppId()
    }

    val apiToken: String by lazy {
        PreferenceUtils.getSendbirdApiToken()
    }

    val baseUrl = "https://api-A1D36AE4-1FFF-4472-B2DA-AA237598ECB1.sendbird.com"

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
                                .header("Api-Token", "d7e6ec94571a0866270703273be79dad19b2713e")
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