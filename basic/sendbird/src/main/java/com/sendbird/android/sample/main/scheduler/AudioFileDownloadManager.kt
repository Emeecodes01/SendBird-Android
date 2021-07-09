package com.sendbird.android.sample.main.scheduler

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AudioFileDownloadManager(private val context: Context) {

    private val sharedPreferences = context
        .getSharedPreferences(DOWNLOAD_AUDIO_PREF_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    fun getDownloadedPath(url: String): String? {
        val downloadMapString = sharedPreferences.getString(DOWNLOADED_URLS, "")
        val typeToken = object: TypeToken<HashMap<String, String>>(){}.type
        val urlToPathMap = gson.fromJson<HashMap<String, String>>(downloadMapString, typeToken)
        return if (urlToPathMap.containsKey(url)) return urlToPathMap[url] else null
    }


    fun saveDownloadedPath(url: String, path: String) {
        val downloads = sharedPreferences.getString(DOWNLOADED_URLS, null)
        downloads?.let {
            val typeToken = object: TypeToken<HashMap<String, String>>(){}.type
            val urlToPathMap = gson.fromJson<HashMap<String, String>>(it, typeToken)
            urlToPathMap[url] = path
            val mapString = gson.toJson(urlToPathMap)
            sharedPreferences.edit { putString(DOWNLOADED_URLS, mapString) }
        } ?: run {
            val map = hashMapOf<String, String>(
                url to path
            )
            val mapString = gson.toJson(map)
            sharedPreferences.edit { putString(DOWNLOADED_URLS, mapString) }
        }
    }


    fun downloadAudio(url: String): Long {
        val downloadRequest = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "/ulesson_tutor_voicechat")
            .setTitle("fdfd")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)


        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(downloadRequest)
    }


    companion object {
        const val DOWNLOADED_URLS = "downloads"
        const val DOWNLOAD_AUDIO_PREF_NAME = "audio_downloads38497@#"
    }
}