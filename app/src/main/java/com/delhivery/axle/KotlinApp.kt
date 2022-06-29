package com.delhivery.axle

import com.delhivery.axle.injection.component.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.moengage.core.DataCenter
import com.moengage.core.MoEngage

/**
 * Kotlin Application, with application injector
 */
class KotlinApp : DaggerApplication() {
  override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
    DaggerAppComponent.builder().create(this)


  override fun onCreate() {
    super.onCreate()
    setupMoEngage()
    createNotificationChannel()
  }

  private fun setupMoEngage() {
    val moEngage = MoEngage.Builder(this, "965N4GFJCV9UF6OBEPETGZR3").setDataCenter(DataCenter.DATA_CENTER_3).build()
   MoEngage.initialise(moEngage)
  }

  private fun createNotificationChannel() {
    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
      val notificationChannel=NotificationChannel(CHANNEL_ID," TOKEN SERVICE CHANNEL",NotificationManager.IMPORTANCE_DEFAULT)
      val manager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(notificationChannel)
    }

  }

  companion object {
    const val CHANNEL_ID = "tokenServiceChannel"
  }
}