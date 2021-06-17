package com.sendbird.android.sample.main

import androidx.multidex.MultiDexApplication
import androidx.multidex.MultiDex
import com.sendbird.android.sample.utils.PreferenceUtils
import com.sendbird.android.SendBird
import com.sendbird.android.sample.utils.PushUtils
import com.sendbird.android.sample.fcm.MyFirebaseMessagingService

abstract class BaseApplication : MultiDexApplication() {
    abstract val apiToken: String
    abstract val sendBirdAppId: String

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        PreferenceUtils.init(applicationContext)
        PreferenceUtils.saveSendBirdId(sendBirdAppId)
        PreferenceUtils.saveSendbirdAPIToken(apiToken)

        SendBird.init(sendBirdAppId, applicationContext)
        PushUtils.registerPushHandler(MyFirebaseMessagingService())
    }

    companion object {
        //    private static final String APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23"; // US-1 Demo
        private const val APP_ID = "58BB27E6-E4EA-4448-83F5-8E03A873AD6A" // US-1 Demo
        const val VERSION = "3.0.40"
    }
}