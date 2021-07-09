package com.sendbird.android.sample.utils

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sendbird.android.GroupChannel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object TextUtils {
    @JvmField
    var THEME_MATH = "mathematics_english"
    fun getGroupChannelTitle(channel: GroupChannel): String {
        val members = channel.members
        return if (members.size < 2 || PreferenceUtils.getUserId().isEmpty()) {
            "No Members"
        } else if (members.size == 2) {
            val names = StringBuffer()
            for (member in members) {
                if (member.userId == PreferenceUtils.getUserId()) {
                    continue
                }
                names.append(", " + member.nickname)
            }
            names.delete(0, 2).toString()
        } else {
            var count = 0
            val names = StringBuffer()
            for (member in members) {
                if (member.userId == PreferenceUtils.getUserId()) {
                    continue
                }
                count++
                names.append(", " + member.nickname)
                if (count >= 10) {
                    break
                }
            }
            names.delete(0, 2).toString()
        }
    }

    /**
     * Calculate MD5
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     */
    @JvmStatic
    @Throws(NoSuchAlgorithmException::class)
    fun generateMD5(data: String): String {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(data.toByteArray())
        val messageDigest = digest.digest()
        val hexString = StringBuffer()
        for (i in messageDigest.indices) hexString.append(
            Integer.toHexString(
                0xFF and messageDigest[i]
                    .toInt()
            )
        )
        return hexString.toString()
    }

    @JvmStatic
    fun isEmpty(text: CharSequence?): Boolean {
        return text == null || text.length == 0
    }

    @JvmStatic
    fun toMap(v: String): MutableMap<String, String> {
        var value = v

        if (value.isJsonString()) {
            val gsonBuilder = GsonBuilder()
//                .registerTypeAdapter(Int::class.java, IntDeserializer())
//                .registerTypeAdapter(Boolean::class.java, IntDeserializer())
//                .registerTypeAdapter(Double::class.java, IntDeserializer())
                .create()

            val typeToken = object: TypeToken<MutableMap<String, String>>(){}.type
            return gsonBuilder.fromJson(value, typeToken)
        }

        value = value.substring(1, value.length - 1)

        val keyValuePairs =
            value.split(",".toRegex()).toTypedArray()

        val map: MutableMap<String, String> = HashMap()

        var previousMapKey = ""
        for (pair in keyValuePairs) {
            val entry = pair.split("=".toRegex()).toTypedArray() //split the pairs to get key and value
            if (entry.size == 1) {
                map[previousMapKey.trim()] = map[previousMapKey.trim()].toString() + "," + entry[0]
            } else {
                map[entry[0].trim { it <= ' ' }] =
                    entry[1].trim { it <= ' ' } //add them to the hashmap and trim whitespaces
                previousMapKey = entry[0]
            }
        }
        return map
    }


    @JvmStatic
    fun isBooleanString(value: String): Boolean {
        return !(value == "pending" || value == "active" || value == "past")
    }

}