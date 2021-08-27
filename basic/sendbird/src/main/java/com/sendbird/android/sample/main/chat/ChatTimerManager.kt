package com.sendbird.android.sample.main.chat

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendbird.android.sample.main.scheduler.SessionStoreManager
import kotlin.math.abs

class ChatTimerManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private val gson = Gson()


    /**
     * Save time until finish
     */
    fun saveTimeUntilFinished(questionId: Int, time: Long) {
        val questionIdToElaspedTime = sharedPreferences.getString(CHAT_TIME, null)

        if (questionIdToElaspedTime != null) {
            val typeToken = object: TypeToken<MutableMap<String, Long>>(){}.type
            val questionIdToElaspedTimeMap =
                gson.fromJson<MutableMap<String, Long>>(questionIdToElaspedTime, typeToken)
            questionIdToElaspedTimeMap["$questionId"] = time
            val string = gson.toJson(questionIdToElaspedTimeMap)
            sharedPreferences.edit { putString(CHAT_TIME, string) }
        }else {
            val map = mutableMapOf<String, Long>()
            map["$questionId"] = time
            val string = gson.toJson(map)
            sharedPreferences.edit { putString(CHAT_TIME, string) }
        }
    }


    fun saveTimeConsolidation(questionId: Int) {
        val idToConsolidation = sharedPreferences.getString(CHAT_TIME_CONSOLIDATIONS, null)

        if (idToConsolidation != null) {

            val cTypeToken = object: TypeToken<MutableMap<String, Long>>(){}.type

            val idToConsolidationMap = gson.fromJson<MutableMap<String, Long>>(idToConsolidation, cTypeToken)

            idToConsolidationMap["$questionId"] = System.currentTimeMillis()

            val string = gson.toJson(idToConsolidationMap)
            sharedPreferences.edit { putString(CHAT_TIME_CONSOLIDATIONS, string) }
        } else {
            val map = mutableMapOf<String, Long>()
            map["$questionId"] = System.currentTimeMillis()
            val string = gson.toJson(map)
            sharedPreferences.edit { putString(CHAT_TIME_CONSOLIDATIONS, string) }
        }
    }


    /**
     * Check if we already have an entry saved
     */
    private fun containsElapsedTimeForId(questionId: Int): Boolean {
        val questionIdToElaspedTime = sharedPreferences.getString(CHAT_TIME, null) ?: return false

        val typeToken = object: TypeToken<MutableMap<String, Long>>(){}.type

        val questionIdToElaspedTimeMap =
            gson.fromJson<MutableMap<String, Long>>(questionIdToElaspedTime, typeToken)

        if (!questionIdToElaspedTimeMap.containsKey("$questionId"))
            return false

        return true
    }


    private fun containsTimeConsolidationForId(questionId: Int): Boolean {
        val idToConsolidation = sharedPreferences.getString(CHAT_TIME_CONSOLIDATIONS, null)
            ?: return false

        val typeToken = object: TypeToken<MutableMap<String, Long>>(){}.type

        val idToConsolidationMap =
            gson.fromJson<MutableMap<String, Long>>(idToConsolidation, typeToken)

        return idToConsolidationMap.containsKey("$questionId")
    }


    /**
     * Get the elapsed time
     */
    fun getTimeUntilFinish(questionId: Int): Long {

        if (containsElapsedTimeForId(questionId)
            && containsTimeConsolidationForId(questionId)) {
            val questionIdToElaspedTime = sharedPreferences.getString(CHAT_TIME, null)
            val idToTimeConsolidation = sharedPreferences.getString(CHAT_TIME_CONSOLIDATIONS, null)

            val typeToken = object: TypeToken<MutableMap<String, Long>>(){}.type
            val questionIdToElaspedTimeMap =
                gson.fromJson<MutableMap<String, Long>>(questionIdToElaspedTime, typeToken)

            val idToTimeConsolidationMap = gson.
            fromJson<MutableMap<String, Long>>(idToTimeConsolidation, typeToken)

            val initCuTime = idToTimeConsolidationMap["$questionId"] ?: System.currentTimeMillis()

            val diff = System.currentTimeMillis() - initCuTime

            val time = questionIdToElaspedTimeMap["$questionId"] ?: 0
            return time - abs(diff)
        }

        return 0
    }


    companion object {
        const val PREF_NAME = "chat-timer-pref00#@"
        const val CHAT_TIME = "chat-times"
        const val CHAT_TIME_CONSOLIDATIONS = "chat-time-consolidation"
    }

}