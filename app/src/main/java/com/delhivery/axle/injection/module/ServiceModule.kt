package com.delhivery.axle.injection.module

import com.delhivery.axle.fcm.DelhiveryFCMService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ServiceModule {

  @ContributesAndroidInjector
  internal abstract fun contributeDelhiveryFCMService(): DelhiveryFCMService
}