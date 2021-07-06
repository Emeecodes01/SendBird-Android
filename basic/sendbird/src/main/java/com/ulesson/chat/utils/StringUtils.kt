package com.ulesson.chat.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringUtils {

    companion object {
        @JvmStatic
        fun String.toMutableMap(): MutableMap<String, Any?> {

            val type = object : TypeToken<HashMap<String, Any?>>() {}.rawType

            val gsonString = try {
                Gson().fromJson(this, type).toString()
            } catch (ignore: Exception) {
                this
            }

            return gsonString.replace("{", "").replace("}", "")
                .split(",")
                .map { it.split("=") }
                .map { it.first().trim() to it.last().trim() }
                .toMap().toMutableMap()
        }

        fun String.chatType(): Boolean {
            val map = this.gsonToMap()
            return map["active"] == "true" || map["active"] == "active"
        }

        private fun String.gsonToMap(): MutableMap<String, Any?> {
            val type = object : TypeToken<HashMap<String, Any?>>() {}.rawType
            return try {
                Gson().fromJson(this, type).toString().toMutableMap()
            } catch (ignore: Exception) {
                this.toMutableMap()
            }
        }
    }

    private fun String.gsonToMap(): MutableMap<String, Any?> {
        val type = object : TypeToken<HashMap<String, Any?>>() {}.rawType
        return try {
            Gson().fromJson(this, type).toString().toMutableMap()
        } catch (ignore: Exception) {
            this.toMutableMap()
        }
    }

    fun String.chatType(): ChatType {

        val map = this.gsonToMap().toMutableMap()
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