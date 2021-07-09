package com.sendbird.android.sample.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.Exception
import java.lang.reflect.Type

fun String.toMutableMap(): MutableMap<String, String> {
    var value = this
    if (value.isJsonString()) {
        val gsonBuilder = GsonBuilder()
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


fun String.isJsonString(): Boolean {
    return try {
        val typeToken = object: TypeToken<MutableMap<String, Any>>(){}.type
        Gson().fromJson<MutableMap<String, Any>>(this, typeToken)
        true
    }catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun String.isBooleanString(): Boolean {
    return !(this == "pending" || this == "active" || this == "past")
}