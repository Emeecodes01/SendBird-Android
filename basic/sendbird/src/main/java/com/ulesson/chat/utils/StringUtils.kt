package com.ulesson.chat.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringUtils {

    companion object {
        @JvmStatic
        fun String.toMutableMap(): MutableMap<String, Any?> {

            val type = object : TypeToken<MutableMap<String, Any?>>() {}.type

            return try {
                Gson().fromJson(this, type)
            } catch (ignore: Exception) {
                this.replace("{", "").replace("}", "")
                    .split(",")
                    .map { it.split("=") }
                    .map { it.first().trim() to it.last().trim() }
                    .toMap().toMutableMap()
            }

        }

        fun String.pendingChatType(): Boolean {
            val map = this.toMutableMap()
            return (map["active"] == "pending" && map["newVersion"] != null)
        }

        fun String.activeChatType(): Boolean {
            val map = this.toMutableMap()
            return (map["active"] == "active" && map["newVersion"] != null) || (map["active"] == "true" && map["newVersion"] == null)
        }

    }

    fun String.chatType(): ChatType {

        val map = this.toMutableMap()
        return if ((map["active"] == "active" && map["newVersion"] != null) || (map["active"] == "true" && map["newVersion"] == null)) {
            ChatType.Active
        } else if (map["active"] == "false" || map["active"] == "past") {
            ChatType.Past
        } else if (map["active"] == "pending") {
            ChatType.PendingChat
        } else {
            ChatType.PendingQuestion
        }
    }

    fun Map<String, Any?>.chatType(): ChatType {

        val map = this
        return if ((map["active"] == "active" && map["newVersion"] != null) || (map["active"] == "true" && map["newVersion"] == null)) {
            ChatType.Active
        } else if (map["active"] == "false" || map["active"] == "past") {
            ChatType.Past
        } else if (map["active"] == "pending") {
            ChatType.PendingChat
        } else {
            ChatType.PendingQuestion
        }
    }

}