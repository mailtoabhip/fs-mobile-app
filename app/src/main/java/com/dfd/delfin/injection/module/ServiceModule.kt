package com.dfd.delfin.injection.module

import com.dfd.delfin.fcm.DelhiveryFCMService
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
internal abstract class ServiceModule {

  @ContributesAndroidInjector
  internal abstract fun contributeDelhiveryFCMService(): DelhiveryFCMService
}