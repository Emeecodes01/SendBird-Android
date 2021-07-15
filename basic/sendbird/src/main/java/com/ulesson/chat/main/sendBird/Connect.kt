package com.ulesson.chat.main.sendBird

import android.text.TextUtils
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.SendBird.UserInfoUpdateHandler
import com.sendbird.android.SendBirdException
import com.sendbird.syncmanager.SendBirdSyncManager
import com.ulesson.chat.main.ConnectionManager
import com.ulesson.chat.main.SyncManagerUtils
import com.ulesson.chat.main.model.UserData
import com.ulesson.chat.utils.PreferenceUtils
import com.ulesson.chat.utils.PushUtils

class Connect {

    fun login(userData: UserData, handler: ConnectHandler) {

        if (TextUtils.isEmpty(userData.id) || TextUtils.isEmpty(userData.nickname)) {
            return
        }

        ConnectionManager.login(userData.id, userData.accessToken) { user, e ->

            if (e == null && PreferenceUtils.getContext() != null) {
                PreferenceUtils.setConnected(true)
                updateCurrentUserInfo(userData.id, userData.nickname, userData.accessToken)

//                PushUtils.unregisterPushTokenForCurrentUser {
//
//                    if (it == null) {
//
//                        PushUtils.refreshPushTokenForCurrentUser(object :
//                            SendBird.RegisterPushTokenWithStatusHandler {
//                            override fun onRegistered(
//                                p0: SendBird.PushTokenRegistrationStatus?,
//                                p1: SendBirdException?
//                            ) {
//                                if (p1 != null) {
//                                    return
//                                }
//
//                            }
//
//                        })
//                    }
//
//                }

                SyncManagerUtils.setup(
                    PreferenceUtils.getContext(),
                    userData.id
                ) { SendBirdSyncManager.getInstance().resumeSync() }
            }
            handler.onConnected(user, e)
        }
    }

    fun refreshChannel(connected: () -> Unit, error: () -> Unit) {
        ConnectionManager.addConnectionManagementHandler("CHANNEL_HANDLER_GROUP_CHANNEL_LIST") { reconnect: Boolean ->
            if (reconnect) {
                connected()
            } else {
                error()
            }
        }
    }

    fun refreshActivity(connected: () -> Unit, error: () -> Unit) {
        ConnectionManager.addConnectionManagementHandler("CONNECTION_HANDLER_GROUP_CHANNEL_ACTIVITY") { reconnect: Boolean ->
            if (reconnect) {
                connected()
                ConnectionManager.removeConnectionManagementHandler("CONNECTION_HANDLER_GROUP_CHANNEL_ACTIVITY")
            } else {
                error()
            }
        }
    }

    private fun updateCurrentUserInfo(userId: String, userNickname: String, accessToken: String) {

        SendBird.updateCurrentUserInfo(userNickname, null, object : UserInfoUpdateHandler {

            override fun onUpdated(e: SendBirdException?) {
                if (e != null) {
                    return
                }
                PreferenceUtils.setUserId(userId)
                PreferenceUtils.setAccessToken(accessToken)
                PreferenceUtils.setNickname(userNickname)
            }

        })
    }

}