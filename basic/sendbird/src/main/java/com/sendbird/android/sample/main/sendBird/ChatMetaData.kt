package com.sendbird.android.sample.main.sendBird

object ChatMetaData {

    const val STATE: String = "state"
    const val SUBJECT: String = "subject"
    const val GRADE: String = "grade"
    const val CHANNELNAME: String = "channel_name"
    const val SESSION_START_TIME = "session_chat_time"

    fun create(
        state: String,
        subject: String,
        grade: String,
        channelName: String,
        sessionStartTime: String
    ): Map<String, String>  =
        mapOf(
            STATE to state,
            SUBJECT to subject,
            GRADE to grade,
            CHANNELNAME to channelName,
            SESSION_START_TIME to sessionStartTime
        )
}