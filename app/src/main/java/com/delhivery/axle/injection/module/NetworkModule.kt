package com.delhivery.axle.injection.module

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.delhivery.axle.api.service.*
import com.delhivery.axle.config.UrlConfig
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.injection.qualifier.ApplicationContext
import com.delhivery.axle.network.ConnectionLiveData
import com.delhivery.axle.network.DelhiveryNetworkInterceptor
import com.google.firebase.perf.network.FirebasePerfOkHttpClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.SECONDS
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
  fun provideGson(): Gson = GsonBuilder().setLenient()
      .create()

  @Provides
  @Singleton
  fun provideChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor =
    ChuckerInterceptor.Builder(context).build()

  /**
   * Provide Http(OKHttp) client
   *
   * @return [OkHttpClient]
   */
  @Provides
  @Singleton
  fun provideOkHttpClient(chuckerInterceptor: ChuckerInterceptor, authInterceptor: DelhiveryNetworkInterceptor): OkHttpClient = OkHttpClient.Builder()
      .connectTimeout(30, SECONDS)
      .readTimeout(30, SECONDS)
      .writeTimeout(15, SECONDS)
      .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
      .addInterceptor(chuckerInterceptor)
      .addInterceptor(authInterceptor)
      .build()

  /**
   * Get retrofit instance for RxJava-based services
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

  /**
   * Get retrofit instance for Coroutines-based services (suspend functions)
   * 
   * Note: No call adapter factory is added. Retrofit 2.6+ has built-in support 
   * for Kotlin coroutines (suspend functions).
   *
   * @param urlConfig
   */
  private fun getRetrofitForCoroutines(
    gson: Gson,
    okHttpClient: OkHttpClient,
    urlConfig: UrlConfig
  ) = Retrofit.Builder()
      .baseUrl(urlConfig.url())
      .addConverterFactory(GsonConverterFactory.create(gson))
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
  ): UMSService = getRetrofit(gson, okHttpClient, UrlConfig.UMS).create(
      UMSService::class.java
  )

  /**
   * TPS service
   */
  @Provides
  @Singleton
  fun provideTPSService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.TPSService).create(
    TPSService::class.java
  )

  /**
   * Provide [UserService]
   */
  @Provides
  @Singleton
  fun provideOrionDataService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.UserService).create(
      UserService::class.java
  )

  /**
   * Provide [CityService]
   */
  @Provides
  @Singleton
  fun provideCityService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.CityService).create(
      CityService::class.java
  )

  /**
   * Provide [TransactionService] for RxJava methods
   * 
   * Note: Uses getRetrofit (with RxJava adapter) for RxJava-based methods.
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
   * Provide [TransactionService] for Coroutines (suspend functions)
   * 
   * Note: Uses getRetrofitForCoroutines (no call adapter) for suspend functions.
   * This is a separate instance from the RxJava version to support both paradigms.
   */
  @Provides
  @Singleton
  @javax.inject.Named("coroutines")
  fun provideTransactionServiceCoroutines(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofitForCoroutines(gson, okHttpClient, UrlConfig.TransactionService).create(
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
   * Provide [TruckService]
   */
  @Provides
  @Singleton
  fun provideTruckService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.TruckService).create(
    TruckService::class.java
  )

  /**
   * Provide [PaymentService]
   */
  @Provides
  @Singleton
  fun providePaymentService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.PayableService).create(
      PaymentService::class.java
  )

  /**
   * Provide [PayableService]
   */
  @Provides
  @Singleton
  fun providePayableService(
          gson: Gson,
          okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.PayableService).create(
          PayableService::class.java
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
    getRetrofit(gson, okHttpClient, UrlConfig.LoadCycleService).create(
        LoadCycleService::class.java
    )

  /**
   * Provide [ExpenseService]
   */
  @Provides
  @Singleton
  fun provideExpenseService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) =
    getRetrofit(gson, okHttpClient, UrlConfig.ExpenseService).create(
        ExpenseService::class.java
    )

  /**
   * Provide [UtilityService]
   */
  @Provides
  @Singleton
  fun provideUtilityService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.UtilityService).create(
      UtilityService::class.java
  )

  /**
   * Provide [OMCService]
   */

  @Provides
  @Singleton
  fun provideOMCService(
      gson: Gson,
      okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.OMCService).create(
          OMCService::class.java
  )

  /**
   * Provide [LoadboardService]
   */

  @Provides
  @Singleton
  fun provideLoadboardService(
          gson: Gson,
          okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.LoadboardService).create(
          LoadBoardService::class.java
  )

  /**
   * Provide [PriceService]
   */

  @Provides
  @Singleton
  fun providePriceService(
          gson: Gson,
          okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.PriceService).create(
          PriceService::class.java
  )

  /**
   * Provide [Recommendation]
   * 
   * Note: Uses getRetrofitForCoroutines because RecommendationService 
   * contains suspend functions for coroutines-based API calls.
   */

  @Provides
  @Singleton
  fun provideRecommendationService(
          gson: Gson,
          okHttpClient: OkHttpClient
  ) = getRetrofitForCoroutines(gson, okHttpClient, UrlConfig.RecommendationService).create(
          RecommendationService::class.java
  )

  /**
   * Provide [SpotBiddingService]
   */
  @Provides
  @Singleton
  fun provideSpotBiddingService(
          gson: Gson,
          okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.SpotBiddingService).create(
          SpotBiddingService::class.java
  )

  /**
   * Provide [FastagService]
   */
  @Provides
  @Singleton
  fun provideFastagService(
          gson: Gson,
          okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.FastagService).create(
          FastagService::class.java
  )

  /**
   * Provide DocumentService for secure document upload/download
   */
  @Provides
  @Singleton
  fun provideDocumentService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.DocumentService).create(
    DocumentService::class.java
  )

  /**
   * Provide InvoiceService for invoice download
   */
  @Provides
  @Singleton
  fun provideInvoiceService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ) = getRetrofit(gson, okHttpClient, UrlConfig.InvoiceService).create(
    InvoiceService::class.java
  )

  /**
   * Provide DocumentUtils for secure document upload/download
   */
  @Provides
  @Singleton
  fun provideDocumentUtils(
    documentService: DocumentService
  ): DocumentUtils = DocumentUtils(documentService)

  /**
   * Provide [FsAuthService] for FS Auth Service (signup, login, profile, token refresh).
   * Uses the coroutines-based Retrofit instance — no RxJava adapter needed.
   */
  @Provides
  @Singleton
  fun provideFsAuthService(
    gson: Gson,
    okHttpClient: OkHttpClient
  ): FsAuthService = getRetrofitForCoroutines(gson, okHttpClient, UrlConfig.FsAuthService).create(
    FsAuthService::class.java
  )
}