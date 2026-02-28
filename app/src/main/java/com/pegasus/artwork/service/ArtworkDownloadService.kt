package com.pegasus.artwork.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.pegasus.artwork.MainActivity
import com.pegasus.artwork.data.local.PreferencesDataStore
import com.pegasus.artwork.domain.model.DownloadProgress
import com.pegasus.artwork.domain.usecase.DownloadAllArtworkUseCase
import com.pegasus.artwork.domain.usecase.ScanRomDirectoryUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ArtworkDownloadService : Service() {

    companion object {
        const val CHANNEL_ID = "artwork_download"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "com.pegasus.artwork.STOP_DOWNLOAD"
    }

    @Inject lateinit var preferencesDataStore: PreferencesDataStore
    @Inject lateinit var scanRomDirectoryUseCase: ScanRomDirectoryUseCase
    @Inject lateinit var downloadAllArtworkUseCase: DownloadAllArtworkUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var downloadJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _progress = MutableStateFlow<DownloadProgress?>(null)
    val progress: StateFlow<DownloadProgress?> = _progress.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    inner class LocalBinder : Binder() {
        fun getService(): ArtworkDownloadService = this@ArtworkDownloadService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopDownload()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification("Preparing download..."))
        acquireWakeLock()
        startDownload()

        return START_NOT_STICKY
    }

    private fun startDownload() {
        if (_isRunning.value) return
        _isRunning.value = true

        downloadJob = serviceScope.launch {
            val uri = preferencesDataStore.romDirectoryUri.first() ?: run {
                stopSelf()
                return@launch
            }

            val systems = scanRomDirectoryUseCase(uri)
            if (systems.isEmpty()) {
                stopSelf()
                return@launch
            }

            val maxThreads = preferencesDataStore.maxThreads.first()

            downloadAllArtworkUseCase(systems, uri, maxThreads).collect { event ->
                _progress.value = event.progress

                updateNotification(
                    "${event.progress.completedRoms}/${event.progress.totalRoms} - " +
                        event.progress.currentRomName,
                )

                if (event.isComplete) {
                    showCompleteNotification(event.progress.completedRoms)
                    stopSelf()
                }
            }
        }
    }

    private fun stopDownload() {
        downloadJob?.cancel()
        _isRunning.value = false
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Artwork Downloads",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows download progress for artwork"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val stopIntent = Intent(this, ArtworkDownloadService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading Artwork")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun showCompleteNotification(count: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Artwork Download Complete")
            .setContentText("Processed $count ROMs")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PegasusCompanion::DownloadWakeLock",
        ).apply {
            acquire(60 * 60 * 1000L) // 1 hour max
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadJob?.cancel()
        releaseWakeLock()
        serviceScope.cancel()
        _isRunning.value = false
    }
}
