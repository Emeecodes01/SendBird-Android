package com.sendbird.utils

class StringUtils {

    companion object {
        @JvmStatic fun String.toMutableMap(): MutableMap<String, Any?> {
            return this.replace("{", "").replace("}", "")
                    .split(",")
                    .map { it.split("=") }
                    .map { it.first().trim() to it.last().trim() }
                    .toMap().toMutableMap()
        }

        fun String.isActive(): Boolean {
            val map = this.toMutableMap()
            return map["active"] == "true"
        }
    }

    fun String.isActive(): Boolean {
        val map = this.toMutableMap()
        return map["active"] == "true"
    }

}