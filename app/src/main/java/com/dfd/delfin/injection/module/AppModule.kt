package com.dfd.delfin.injection.module

import androidx.room.Room
import android.content.Context
import com.dfd.delfin.KotlinApp
import com.dfd.delfin.database.AppDatabase
import com.dfd.delfin.injection.qualifier.ApplicationContext
import com.dfd.delfin.utils.Config
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
  fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
    Room.databaseBuilder(
        context, AppDatabase::class.java, Config.AppDatabaseName
    ).fallbackToDestructiveMigration().build()
}