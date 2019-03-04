package com.delhivery.orion.injection.module

import android.arch.persistence.room.Room
import android.content.Context
import com.delhivery.orion.KotlinApp
import com.delhivery.orion.database.AppDatabase
import com.delhivery.orion.injection.qualifier.ApplicationContext
import com.delhivery.orion.utils.Config
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    /**
     * Provide Application Context, when [ApplicationContext] annotation is marked
     */
    @Provides
    @ApplicationContext
    fun provideAppContext(app: KotlinApp): Context = app.applicationContext

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, Config.AppDatabaseName).build()
}