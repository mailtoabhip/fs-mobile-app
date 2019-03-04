package com.delhivery.orion.injection.component

import com.delhivery.orion.KotlinApp
import com.delhivery.orion.injection.module.ActivityBindingModule
import com.delhivery.orion.injection.module.AppModule
import com.delhivery.orion.injection.module.NetworkModule
import com.delhivery.orion.injection.module.ViewModelFactoryModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelFactoryModule::class, AndroidSupportInjectionModule::class, ActivityBindingModule::class, NetworkModule::class])
interface AppComponent : AndroidInjector<KotlinApp> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<KotlinApp>()
}