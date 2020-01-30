package com.delhivery.axle.injection.module

import android.content.Context
import com.delhivery.axle.api.BidService
import com.delhivery.axle.api.CityService
import com.delhivery.axle.api.FuelService
import com.delhivery.axle.api.LoadCycleService
import com.delhivery.axle.api.NotificationService
import com.delhivery.axle.api.PaymentService
import com.delhivery.axle.api.TransactionService
import com.delhivery.axle.api.TripService
import com.delhivery.axle.api.UMSService
import com.delhivery.axle.api.UserService
import com.delhivery.axle.api.WalletService
import com.delhivery.axle.api.WarehouseService
import com.delhivery.axle.config.UrlConfig
import com.delhivery.axle.injection.qualifier.ApplicationContext
import com.delhivery.axle.network.ConnectionLiveData
import com.delhivery.axle.network.DelhiveryNetworkInterceptor
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

  /**
   * Bid service
   */
  @Provides
  @Singleton
  fun provideBidService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.BidService).create(BidService::class.java)

  /**
   * Provide [UserService]
   */
  @Provides
  @Singleton
  fun provideOrionDataService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.UserService).create(UserService::class.java)

  /**
   * Provide [CityService]
   */
  @Provides
  @Singleton
  fun provideCityService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.CityService).create(CityService::class.java)

  /**
   * Provide [TransactionService]
   */
  @Provides
  @Singleton
  fun provideTransactionService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.TransactionService).create(
      TransactionService::class.java
  )

  /**
   * Provide [TripService]
   */
  @Provides
  @Singleton
  fun provideTripService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.TripService).create(
      TripService::class.java
  )

  /**
   * Provide [PaymentService]
   */
  @Provides
  @Singleton
  fun providePaymentService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.PaymentService).create(
      PaymentService::class.java
  )

  /**
   * Provide [WarehouseService]
   */
  @Provides
  @Singleton
  fun provideWarehouseService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.WarehouseService).create(
      WarehouseService::class.java
  )

  /**
   * Provide [NotificationService]
   */
  @Provides
  @Singleton
  fun provideNotificationService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.NotificationService).create(
      NotificationService::class.java
  )

  /**
   * Provide [WalletService]
   */
  @Provides
  @Singleton
  fun provideWalletService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.WalletService).create(
      WalletService::class.java
  )

  /**
   * Provide [FuelService]
   */
  @Provides
  @Singleton
  fun provideFuelService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.FuelService).create(
      FuelService::class.java
  )

  /**
   * Provide [LoadCycleService]
   */
  @Provides
  @Singleton
  fun provideLoadCycleService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) =
    getRetrofit(gson, okHttpClient, UrlConfig.LoadCycleService).create(LoadCycleService::class.java)
}