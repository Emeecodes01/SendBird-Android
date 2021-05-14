package com.sendbird.main.sendBird

import com.sendbird.main.ConnectionManager
import com.sendbird.main.SyncManagerUtils
import com.sendbird.network.NetworkRequest
import com.sendbird.network.createUser.ConnectUserRequest
import com.sendbird.network.createUser.UpdateUserRequest
import com.sendbird.network.createUser.UserResponse
import com.sendbird.syncmanager.SendBirdSyncManager
import com.sendbird.utils.PreferenceUtils

class User {

    private val networkRequest = NetworkRequest()

    fun disconnectUser() {
        ConnectionManager.logout {}
    }

    fun connectUser(userData: ConnectUserRequest, accessToken: String?, userResponse: (UserResponse) -> Unit, errorResponse: (ErrorData) -> Unit, updateAccessToken: (String?) -> Unit) {

        if (accessToken.isNullOrEmpty()) {

            networkRequest.createUser(userData, {
                userResponse(it)
            }, {

                val loginData = UserData(userData.user_id, userData.nickname)

                updateUser(loginData, {
                    userResponse(it)
                }, {
                    errorResponse(it)
                }, {
                    updateAccessToken(it)
                })

            })

        } else {

            if (ConnectionManager.isLogin() && PreferenceUtils.getUserId() != null && PreferenceUtils.getContext() != null) {

                SyncManagerUtils.setup(PreferenceUtils.getContext(), userData.user_id) { SendBirdSyncManager.getInstance().resumeSync() }

                userResponse(UserResponse(userData.user_id, userData.nickname, userData.profile_url, accessToken))

            } else {
                val loginData = UserData(userData.user_id, userData.nickname, accessToken)

                Connect().login(loginData) { user, loginError ->

                    user?.let {
                        userResponse(UserResponse(it.userId, it.nickname, it.profileUrl, accessToken))
                        updateAccessToken(null)
                    }

                    if (loginError != null) {
                        updateUser(loginData, {
                            userResponse(it)
                        }, {
                            errorResponse(it)
                        }, {
                            updateAccessToken(it)
                        })
                    }

                }
            }

        }
    }

    private fun updateUser(userData: UserData, userResponse: (UserResponse) -> Unit, errorResponse: (ErrorData) -> Unit, updateAccessToken: (String?) -> Unit) {

        networkRequest.updateUser(UpdateUserRequest(userData.id, true), { userResponse ->

            val loginData = UserData(userData.id, userData.nickname, userResponse.access_token)

            Connect().login(loginData) { user, loginError ->

                user?.let {
                    userResponse(UserResponse(it.userId, it.nickname, it.profileUrl, ""))
                    updateAccessToken(userResponse.access_token)
                } ?: kotlin.run {
                    errorResponse(ErrorData(loginError.message, loginError.code, true))
                }
            }
        }, {
            errorResponse(ErrorData(it, 0, true))
        })
    }

}