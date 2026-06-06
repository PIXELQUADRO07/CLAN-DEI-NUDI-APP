package com.example.CDN.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.CDN.MainActivity
import com.example.CDN.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class GitHubUpdateService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var checkJob: Job? = null
    private val client = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "GitHubUpdateService: onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "GitHubUpdateService: onStartCommand with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> startChecking()
            ACTION_STOP -> stopChecking()
            ACTION_CHECK_NOW -> triggerManualCheck()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startChecking() {
        if (checkJob?.isActive == true) return

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val intervalMs = prefs.getLong(KEY_INTERVAL, DEFAULT_INTERVAL)

        checkJob = serviceScope.launch {
            while (isActive) {
                performGitHubCheck()
                delay(intervalMs)
            }
        }
    }

    private fun stopChecking() {
        checkJob?.cancel()
        Log.d(TAG, "GitHubUpdateService: stopped periodic checks")
    }

    private fun triggerManualCheck() {
        serviceScope.launch {
            performGitHubCheck()
        }
    }

    private suspend fun performGitHubCheck() {
        // HARDCODED REPO AS REQUESTED
        val owner = "PIXELQUADRO07"
        val repo = "CLAN-DEI-NUDI-APP"
        
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentVersion = prefs.getString(KEY_CURRENT_VERSION, DEFAULT_VERSION_TAG) ?: DEFAULT_VERSION_TAG

        Log.d(TAG, "Checking GitHub releases for repo: $owner/$repo. Current App Version: $currentVersion")

        // Broadcast checking state
        broadcastState(STATE_CHECKING, owner, repo, currentVersion)

        val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "ClanDeiNudi-App-UpdateChecker")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (bodyString != null) {
                        val json = JSONObject(bodyString)
                        val tagName = json.optString("tag_name", "")
                        val releaseName = json.optString("name", "")
                        val changelog = json.optString("body", "Nessuna nota descrittiva.")
                        val htmlUrl = json.optString("html_url", "")

                        Log.d(TAG, "Found GitHub release: $tagName ($releaseName)")

                        // Simple version comparison: if the tag is different from current, trigger update
                        val cleanCurrent = currentVersion.trim().lowercase().removePrefix("v")
                        val cleanTag = tagName.trim().lowercase().removePrefix("v")

                        if (cleanTag.isNotEmpty() && cleanTag != cleanCurrent) {
                            Log.d(TAG, "Newer version detected! Tag: $cleanTag, App: $cleanCurrent")
                            saveDetectedRelease(tagName, releaseName, changelog, htmlUrl)
                            triggerSystemNotification(tagName, releaseName)
                            broadcastState(STATE_UPDATE_FOUND, owner, repo, currentVersion, tagName, releaseName, changelog, htmlUrl)
                        } else {
                            Log.d(TAG, "Current version is up-to-date with GitHub ($cleanCurrent == $cleanTag)")
                            broadcastState(STATE_UP_TO_DATE, owner, repo, currentVersion, tagName, releaseName, changelog, htmlUrl)
                        }
                    } else {
                        throw IOException("Response body was null")
                    }
                } else {
                    val code = response.code
                    throw IOException("HTTP Error $code: ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "GitHub check failed: ${e.localizedMessage}")
            broadcastState(STATE_ERROR, owner, repo, currentVersion, errorMessage = e.localizedMessage)
        }
    }

    private fun saveDetectedRelease(tag: String, name: String, notes: String, url: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LATEST_DETECTED_TAG, tag)
            .putString(KEY_LATEST_DETECTED_NAME, name)
            .putString(KEY_LATEST_DETECTED_NOTES, notes)
            .putString(KEY_LATEST_DETECTED_URL, url)
            .apply()
    }

    private fun triggerSystemNotification(tagName: String, releaseName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "settings")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // System launcher fallback
            .setContentTitle("AGGIORNAMENTO CLAN DISPONIBILE")
            .setContentText("Trovata la release $tagName ($releaseName) su GitHub. Clicca qui per scaricarla.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Nuova versione Rilevata sul GitHub\n" +
                "Versione: $tagName ($releaseName)\n" +
                "Scarica ed installa la patch protetta direttamente dal terminale."
            ))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifiche Aggiornamenti Clan"
            val descriptionText = "Informa del rilascio di nuovi firmware e patch sulla rete del Clan dei Nudi GitHub"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun broadcastState(
        status: String,
        owner: String,
        repo: String,
        currentVer: String,
        latestTag: String = "",
        latestName: String = "",
        notes: String = "",
        url: String = "",
        errorMessage: String? = null
    ) {
        val intent = Intent(ACTION_UPDATE_STATUS).apply {
            putExtra(EXTRA_STATUS, status)
            putExtra(EXTRA_OWNER, owner)
            putExtra(EXTRA_REPO, repo)
            putExtra(EXTRA_CURRENT_VER, currentVer)
            putExtra(EXTRA_LATEST_TAG, latestTag)
            putExtra(EXTRA_LATEST_NAME, latestName)
            putExtra(EXTRA_NOTES, notes)
            putExtra(EXTRA_URL, url)
            if (errorMessage != null) {
                putExtra(EXTRA_ERROR, errorMessage)
            }
        }
        sendBroadcast(intent)
        Log.d(TAG, "Broadcast update state: status=$status for $owner/$repo")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "GitHubUpdateService: onDestroy/Scope cancelled")
    }

    companion object {
        const val TAG = "GitHubUpdateService"
        
        // Actions
        const val ACTION_START = "com.example.CDN.service.action.START"
        const val ACTION_STOP = "com.example.CDN.service.action.STOP"
        const val ACTION_CHECK_NOW = "com.example.CDN.service.action.CHECK_NOW"

        // Broadcast channel
        const val ACTION_UPDATE_STATUS = "com.example.CDN.service.action.UPDATE_STATUS"
        
        // Extras
        const val EXTRA_STATUS = "status"
        const val EXTRA_OWNER = "owner"
        const val EXTRA_REPO = "repo"
        const val EXTRA_CURRENT_VER = "current_ver"
        const val EXTRA_LATEST_TAG = "latest_tag"
        const val EXTRA_LATEST_NAME = "latest_name"
        const val EXTRA_NOTES = "notes"
        const val EXTRA_URL = "url"
        const val EXTRA_ERROR = "error"

        // Status codes
        const val STATE_IDLE = "IDLE"
        const val STATE_CHECKING = "CHECKING"
        const val STATE_UPDATE_FOUND = "UPDATE_FOUND"
        const val STATE_UP_TO_DATE = "UP_TO_DATE"
        const val STATE_ERROR = "ERROR"

        // SharedPreferences Keys
        const val PREFS_NAME = "clan_github_updater_prefs"
        const val KEY_OWNER = "owner"
        const val KEY_REPO = "repo"
        const val KEY_INTERVAL = "interval"
        const val KEY_CURRENT_VERSION = "current_version"
        
        // Saved state keys
        const val KEY_LATEST_DETECTED_TAG = "detected_tag"
        const val KEY_LATEST_DETECTED_NAME = "detected_name"
        const val KEY_LATEST_DETECTED_NOTES = "detected_notes"
        const val KEY_LATEST_DETECTED_URL = "detected_url"

        // Default values
        const val DEFAULT_OWNER = "PIXELQUADRO07"
        const val DEFAULT_REPO = "CLAN-DEI-NUDI-APP"
        const val DEFAULT_VERSION_TAG = "v2.0.4"
        const val DEFAULT_INTERVAL = 30000L // 30 seconds for immediate responsiveness in app

        private const val CHANNEL_ID = "clan_github_updates"
        private const val NOTIFICATION_ID = 4096

        // Easy triggers
        fun start(context: Context) {
            val intent = Intent(context, GitHubUpdateService::class.java).apply {
                action = ACTION_START
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, GitHubUpdateService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun checkNow(context: Context) {
            val intent = Intent(context, GitHubUpdateService::class.java).apply {
                action = ACTION_CHECK_NOW
            }
            context.startService(intent)
        }
    }
}
