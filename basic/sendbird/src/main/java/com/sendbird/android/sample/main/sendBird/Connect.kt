package com.sendbird.android.sample.main.sendBird

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.sendbird.android.*
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.SendBird.UserInfoUpdateHandler
import com.sendbird.android.sample.fcm.MyFirebaseMessagingService
import com.sendbird.android.sample.main.ConnectionManager
import com.sendbird.android.sample.network.NetworkRequest
import com.sendbird.android.sample.utils.PreferenceUtils
import com.sendbird.android.sample.utils.PushUtils
import com.sendbird.android.sample.utils.SyncManagerUtils
import com.sendbird.syncmanager.SendBirdSyncManager
import com.sendbird.syncmanager.handler.CompletionHandler

class Connect {

    /**
     * Login into Send bird using the USERDATA which contains the userid, nickname and access token
     */
    fun login(context: Context, userData: UserData, handler: ConnectHandler) {

        if (TextUtils.isEmpty(userData.id) || TextUtils.isEmpty(userData.nickname)) {
            return
        }

        ConnectionManager.login(userData.id, userData.accessToken, ConnectHandler { user, e: SendBirdException? ->

            if (e != null) {
                Log.d("okh", e.message + "")

            } else {
                PreferenceUtils.setConnected(true)

                // Update the user's nickname
                updateCurrentUserInfo(userData.id, userData.nickname)
                PushUtils.registerPushHandler(MyFirebaseMessagingService())

                //init sync manager
                SyncManagerUtils.setup(context, userData.id) {
                    if (it != null) {
                        it.printStackTrace()
                    }

                    SendBirdSyncManager.getInstance().resumeSync();
                }
            }
            handler.onConnected(user, e)
        })
    }

    private fun updateCurrentUserInfo(userId: String, userNickname: String) {

        SendBird.updateCurrentUserInfo(userNickname, null, object : UserInfoUpdateHandler {

            override fun onUpdated(e: SendBirdException?) {
                if (e != null) {
                    return
                }

                PreferenceUtils.setUserId(userId)
                PreferenceUtils.setNickname(userNickname)
            }

        })
    }

}