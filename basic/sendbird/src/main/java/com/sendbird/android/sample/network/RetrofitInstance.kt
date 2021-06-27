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

    val baseUrl = "https://api-$appId.sendbird.com"

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
                                .header("Api-Token", apiToken)
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


class UlessonRetrofitInstance {

    private val deviceId: String by lazy {
        PreferenceUtils.getDeviceId()
    }

    private val ulessonApiToken: String by lazy {
        PreferenceUtils.getUlessonApiToken()
    }

    private val baseUrl: String by lazy {
        PreferenceUtils.getBaseUrl()
    }

    //private val baseUrl = "https://staging-tutor-backend.ulesson.com"
    //private val baseUrlUAT = "https://uat-tutor-backend.ulesson.com/"
    //private val baseUrlProd = ""

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
                        .addHeader("device-uuid", deviceId)
                        .addHeader("Authorization", "Bearer $ulessonApiToken")

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