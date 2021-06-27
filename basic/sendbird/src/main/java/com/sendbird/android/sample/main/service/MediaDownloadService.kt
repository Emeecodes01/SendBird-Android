package com.sendbird.android.sample.main.service

import android.app.Notification
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.util.concurrent.Executor


/*class MediaDownloadService: DownloadService(){


    override fun getDownloadManager(): DownloadManager {
        // Note: This should be a singleton in your app.
        // Note: This should be a singleton in your app.
        databaseProvider = ExoDatabaseProvider(context)

// A download cache should not evict media, so should use a NoopCacheEvictor.

// A download cache should not evict media, so should use a NoopCacheEvictor.
        downloadCache = SimpleCache (
            downloadDirectory,
            NoOpCacheEvictor(),
            databaseProvider
        )

// Create a factory for reading the data from the network.

// Create a factory for reading the data from the network.
        dataSourceFactory = DefaultHttpDataSourceFactory()

// Choose an executor for downloading data. Using Runnable::run will cause each download task to
// download data on its own thread. Passing an executor that uses multiple threads will speed up
// download tasks that can be split into smaller parts for parallel execution. Applications that
// already have an executor for background downloads may wish to reuse their existing executor.

// Choose an executor for downloading data. Using Runnable::run will cause each download task to
// download data on its own thread. Passing an executor that uses multiple threads will speed up
// download tasks that can be split into smaller parts for parallel execution. Applications that
// already have an executor for background downloads may wish to reuse their existing executor.
        val downloadExecutor = Executor { obj: Runnable -> obj.run() }

// Create the download manager.

// Create the download manager.
        downloadManager = DownloadManager(
            context,
            databaseProvider,
            downloadCache,
            dataSourceFactory,
            downloadExecutor
        )

// Optionally, setters can be called to configure the download manager.

// Optionally, setters can be called to configure the download manager.
        downloadManager.requirements = requirements
        downloadManager.maxParallelDownloads = 3
        return downloadManager
    }

    override fun getScheduler(): Scheduler? {
        TODO("Not yet implemented")
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        TODO("Not yet implemented")
    }
}*/