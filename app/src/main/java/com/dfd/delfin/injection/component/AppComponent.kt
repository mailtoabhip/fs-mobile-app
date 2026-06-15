package com.dfd.delfin.injection.component

import com.dfd.delfin.KotlinApp
import com.dfd.delfin.injection.module.ActivityBindingModule
import com.dfd.delfin.injection.module.AppModule
import com.dfd.delfin.injection.module.CoroutineModule
import com.dfd.delfin.injection.module.NetworkModule
import com.dfd.delfin.injection.module.ServiceModule
import com.dfd.delfin.injection.module.ViewModelFactoryModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AppModule::class, ViewModelFactoryModule::class, AndroidSupportInjectionModule::class,
      ActivityBindingModule::class, NetworkModule::class, ServiceModule::class, CoroutineModule::class]
)
interface AppComponent : AndroidInjector<KotlinApp> {
  /*@Component.Builder
  abstract class Builder : AndroidInjector.Builder<KotlinApp>()*/

  @Component.Factory
  abstract class Factory: AndroidInjector.Factory<KotlinApp>
}