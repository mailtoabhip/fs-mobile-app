package com.delhivery.axle

import com.delhivery.axle.injection.component.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

/**
 * Kotlin Application, with application injector
 */
class KotlinApp : DaggerApplication() {
  override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
    DaggerAppComponent.builder().create(this)
}