package com.delhivery.axle.injection.component

import com.delhivery.axle.KotlinApp
import com.delhivery.axle.injection.module.ActivityBindingModule
import com.delhivery.axle.injection.module.AppModule
import com.delhivery.axle.injection.module.NetworkModule
import com.delhivery.axle.injection.module.ServiceModule
import com.delhivery.axle.injection.module.ViewModelFactoryModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AppModule::class, ViewModelFactoryModule::class, AndroidSupportInjectionModule::class,
      ActivityBindingModule::class, NetworkModule::class, ServiceModule::class]
)
interface AppComponent : AndroidInjector<KotlinApp> {
  /*@Component.Builder
  abstract class Builder : AndroidInjector.Builder<KotlinApp>()*/

  @Component.Factory
  abstract class Factory: AndroidInjector.Factory<KotlinApp>
}