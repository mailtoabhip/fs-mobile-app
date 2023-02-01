package com.delhivery.axle.tokenExpiryHandling

import android.app.*
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.KotlinApp.Companion.CHANNEL_ID
import com.delhivery.axle.R
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig.Flag
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import org.json.JSONObject
import java.io.IOException
import java.util.*
import javax.inject.Inject


class RefreshAuthTokenService : Service(){
    private val client = OkHttpClient()

    @Inject
    lateinit var userPrefs: UserPrefs

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        Log.d("prefs","Service Check")
        val notificationIntent = Intent(this, HomeActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            else PendingIntent.FLAG_ONE_SHOT)

        val title: String = when (BuildConfig.FLAVOR) {
            "production" -> "Axle is running"
            else -> "Dev Axle is running"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Axle App is running in background")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()
        notification.flags = Notification.FLAG_NO_CLEAR
        startForeground(1, notification)

    }


    override fun onBind(p0: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Handler().post{
            validateAndRefreshToken() }

        onTaskRemoved(intent)

        return START_STICKY
    }

    private fun validateAndRefreshToken() {
        if (userPrefs.jwtToken != null) {
            Log.d("prefs","service2")
            if (BuildConfig.FLAVOR == "development" || BuildConfig.FLAVOR == "uat") {
                refreshToken("https://api-stage-ums.delhivery.com/v2/refresh_token/?force=1")
            } else {
                refreshToken("https://api-ums.delhivery.com/v2/refresh_token/?force=1")
            }
        }
    }

    private fun refreshToken(url: String) {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + userPrefs.jwtToken)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {

            }

            override fun onResponse(response: Response?) {
                try {
                    val strResponse = response?.body()?.string()
                    val json = JSONObject(strResponse)
                    if (!json.isNull("jwt")) {
                        val jwtToken = json.optString("jwt")
                        if (jwtToken.isNotNullOrEmpty()){
                            userPrefs.jwtToken = jwtToken
                        }
                    }
                    stopForeground(true)
                    stopService(Intent(applicationContext,RefreshAuthTokenService::class.java))
                    WorkManager.getInstance().cancelUniqueWork(RefreshTokenWorker.WORK_NAME)
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    fun restartService() {
        if (userPrefs.jwtToken == null) {
            stopForeground(true)
            stopService(Intent(applicationContext,RefreshAuthTokenService::class.java))
            WorkManager.getInstance().cancelUniqueWork(RefreshTokenWorker.WORK_NAME)
        }
        else {
            val restartServiceIntent = Intent(applicationContext, this.javaClass)
            restartServiceIntent.setPackage(packageName)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartServiceIntent)
            } else {
                startService(restartServiceIntent)
            }
        }
    }

}