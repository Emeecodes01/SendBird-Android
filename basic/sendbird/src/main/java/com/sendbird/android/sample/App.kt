package com.sendbird.android.sample

import com.sendbird.android.sample.main.BaseApplication

class App(override val apiToken: String) : BaseApplication() {

    override val sendBirdAppId: String
        get() = TODO("Not yet implemented")


    override fun onCreate() {
        super.onCreate()

    }
}