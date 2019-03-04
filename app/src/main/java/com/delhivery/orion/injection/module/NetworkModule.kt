package com.delhivery.orion.injection.module

import android.content.Context
import com.delhivery.orion.api.UMSService
import com.delhivery.orion.config.UrlConfig
import com.delhivery.orion.injection.qualifier.ApplicationContext
import com.delhivery.orion.network.ConnectionLiveData
import com.delhivery.orion.network.DelhiveryNetworkInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Network Module contains all dependencies related to Network
 */
@Module
class NetworkModule {

  /**
   * Provides Connection Live Data for listening to internet connection updates
   *
   * @return [ConnectionLiveData]
   */
  @Provides
  @Singleton
  fun provideConnectionLiveData(@ApplicationContext context: Context): ConnectionLiveData =
    ConnectionLiveData(context)

  /**
   * Provide GSON serialization,
   * Inject it where ever required
   * and avoid creating another instances of Gson
   *
   * @return [Gson]
   */
  @Provides
  @Singleton
  fun provideGson(): Gson = GsonBuilder().setLenient().create()

  /**
   * Provide Http(OKHttp) client
   *
   * @return [OkHttpClient]
   */
  @Provides
  @Singleton
  fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
      .addInterceptor(DelhiveryNetworkInterceptor.instance)
      .build()

  /**
   * Get retrofit instance for
   *
   * @param urlConfig
   */
  private fun getRetrofit(
    gson: Gson,
    okHttpClient: OkHttpClient,
    urlConfig: UrlConfig
  ) = Retrofit.Builder()
      .baseUrl(urlConfig.url())
      .addConverterFactory(GsonConverterFactory.create(gson))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(okHttpClient)
      .build()

  /*/* Services to be placed here */*/

  /**
   * Provide UMSService
   *
   * @return [UMSService]
   */
  @Provides
  @Singleton
  fun provideHQService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ): UMSService = getRetrofit(gson, okHttpClient, UrlConfig.UMS).create(UMSService::class.java)
}