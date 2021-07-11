package com.sendbird.android.sample.main.scheduler

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class AudioFileDownloadManager(private val mContext: Context) {

    private val sharedPreferences = mContext
        .getSharedPreferences(DOWNLOAD_AUDIO_PREF_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    fun getDownloadedPath(url: String): String? {
        val downloadMapString = sharedPreferences.getString(DOWNLOADED_URLS, "")
        val typeToken = object: TypeToken<HashMap<String, String>>(){}.type
        val urlToPathMap = gson.fromJson<HashMap<String, String>>(downloadMapString, typeToken)
        return if (urlToPathMap.containsKey(url)) return urlToPathMap[url] else null
    }

    fun hasDownloadedAudio(url: String): Boolean {
        val downloaded = getDownloadedPath(url)
        return if (downloaded == null) return false else true
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


    fun downloadAudio(url: String, name: String, onLoading: () -> Unit, onCompleted: () -> Unit) {
        var currentDownloadId: Long = -1
        val path = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/ulesson_tutor_voicechat"

        val broadcastReceiver: BroadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (currentDownloadId == id) {
                    //download complete
                    mContext.unregisterReceiver(this)
                    saveDownloadedPath(url, path)
                    onCompleted.invoke()
                }
            }
        }

        val downloadRequest = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, "/ulesson_tutor_voicechat")
            .setTitle(name)
            .setDescription("Downloading")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)


        val downloadManager = mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        currentDownloadId = downloadManager.enqueue(downloadRequest)

        onLoading.invoke()

        mContext.registerReceiver(broadcastReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }



    companion object {
        const val DOWNLOADED_URLS = "downloads"
        const val DOWNLOAD_AUDIO_PREF_NAME = "audio_downloads38497@#"
    }
}