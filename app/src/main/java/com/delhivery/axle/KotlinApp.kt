package com.delhivery.axle

import com.delhivery.axle.injection.component.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.delhivery.axle.R.drawable
import com.moengage.core.DataCenter
import com.moengage.core.MoEngage
import com.moengage.core.config.FcmConfig
import com.moengage.core.config.NotificationConfig
import androidx.work.Configuration
import androidx.work.WorkManager
import com.delhivery.axle.injection.module.DaggerWorkerFactory
import javax.inject.Inject

/**
 * Kotlin Application, with application injector
 */
class KotlinApp : DaggerApplication() {
  override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
    DaggerAppComponent.builder().create(this)

  @Inject
  lateinit var workerFactory: DaggerWorkerFactory


  override fun onCreate() {
    super.onCreate()
    setupMoEngage()
    createNotificationChannel()
    WorkManager.initialize(
            this,
            Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build()
    )
  }

  private fun setupMoEngage() {
    val isSdkEnabled = !(BuildConfig.FLAVOR == "development" || BuildConfig.FLAVOR == "uat")
    val moEngage = MoEngage.Builder(this, "965N4GFJCV9UF6OBEPETGZR3").setDataCenter(DataCenter.DATA_CENTER_3)
        .configureNotificationMetaData(NotificationConfig(R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.color.colorPrimary, null, true, isBuildingBackStackEnabled = false, isLargeIconDisplayEnabled = true))
        .configureFcm(FcmConfig(false)) .build()
    MoEngage.initialise(moEngage,true)
  }
  private fun createNotificationChannel() {
    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
      val notificationChannel=NotificationChannel(CHANNEL_ID,"TOKEN SERVICE CHANNEL",NotificationManager.IMPORTANCE_DEFAULT)
      val manager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(notificationChannel)
    }

  }

  companion object {
    const val CHANNEL_ID = "tokenServiceChannel"
  }
}