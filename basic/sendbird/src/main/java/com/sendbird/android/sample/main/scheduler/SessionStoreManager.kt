package com.sendbird.android.sample.main.scheduler

import android.content.Context
import android.content.SharedPreferences
import com.sendbird.android.sample.utils.toMutableMap

class SessionStoreManager(val context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSessionId(id: Int?) {
        val sessions = sharedPreferences.getString(CHAT_SESSIONS, null)?.toMutableMap()
        sessions?.let { map ->
            if (!isSessionAvaliable(id)) {
                map["$id"] = true
            }
            sharedPreferences.edit().putString(CHAT_SESSIONS, map.toString()).apply()
        }?: run {
            val hashMap: MutableMap<String, Any> = mutableMapOf()
            hashMap.put("$id", true)
            sharedPreferences.edit().putString(CHAT_SESSIONS, hashMap.toString()).apply()
        }
    }


    fun isSessionAvaliable(sessionId: Int?): Boolean {
        if (sessionId == null) {
            throw IllegalArgumentException("SessionID cannot be null")
        }

        val sessions = sharedPreferences.getString(CHAT_SESSIONS, null)?.toMutableMap()
        return sessions?.let {
            try {
                it["$sessionId"].toString().toBoolean()
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

        } ?: false
    }



    companion object {
        const val PREF_NAME = "pref-002343!@3"
        const val CHAT_SESSIONS = "chat_session"
    }
}