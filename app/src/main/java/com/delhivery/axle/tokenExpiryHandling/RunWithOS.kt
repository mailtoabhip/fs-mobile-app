package com.delhivery.axle.tokenExpiryHandling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class RunWithOs : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
//        if(p1!!.action.equals("android.intent.action.BOOT_COMPLETED",true)){
//            val constraints = Constraints.Builder()
//                .build()
//
//            val repeatingRequest
//                    = PeriodicWorkRequestBuilder<RefreshTokenWorker>(16, TimeUnit.MINUTES)
//                .setConstraints(constraints)
//                .build()
//
//            WorkManager.getInstance().enqueueUniquePeriodicWork(
//                RefreshTokenWorker.WORK_NAME,
//                ExistingPeriodicWorkPolicy.KEEP,
//                repeatingRequest)
//        }

    }
}