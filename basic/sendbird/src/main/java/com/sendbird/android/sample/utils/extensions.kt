package com.sendbird.android.sample.utils

fun String.toMutableMap(): MutableMap<String, Any> {
    var value = this
    value = value.substring(1, value.length - 1)

    val keyValuePairs =
        value.split(",".toRegex()).toTypedArray()

    val map: MutableMap<String, Any> = HashMap()

    for (pair in keyValuePairs) {
        val entry = pair.split("=".toRegex()).toTypedArray() //split the pairs to get key and value
        map[entry[0].trim { it <= ' ' }] =
            entry[1].trim { it <= ' ' } //add them to the hashmap and trim whitespaces
    }

    return map
}