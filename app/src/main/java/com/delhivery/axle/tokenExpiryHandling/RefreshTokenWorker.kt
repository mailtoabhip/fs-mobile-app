package com.delhivery.axle.tokenExpiryHandling

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import retrofit2.HttpException

class RefreshTokenWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    companion object {
        const val WORK_NAME = "RefreshTokenWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d("prefs","Work Started")
            if(!isMyServiceRunning(RefreshAuthTokenService::class.java)) {
                val intent = Intent(applicationContext, RefreshAuthTokenService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    applicationContext.startForegroundService(intent)
                } else {
                    applicationContext.startService(intent)
                }
            }
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager = applicationContext.getSystemService(Service.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.getClassName()) {
                return true
            }
        }
        return false
    }


}