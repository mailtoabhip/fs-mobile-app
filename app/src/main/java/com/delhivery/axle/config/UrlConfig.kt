package com.delhivery.axle.config

import com.delhivery.axle.BuildConfig

/**
 * URL Config for all services
 */
enum class UrlConfig(
  private val prod: String,
  private val dev: String
) {
  UMS(
      "https://api-ums.delhivery.com",
      "https://api-stage-ums.delhivery.com"
  ),
  BidService(
      "https://orion-bid-api.delhivery.com",
      "https://orion-bid-api-dev.delhivery.com"
  ),
  UserService(
      "https://orion-user-api.delhivery.com",
      "https://orion-user-api-dev.delhivery.com"
  ),
  TransactionService(
      "http://orion-transaction-api.delhivery.com",
      "http://orion-transaction-api-dev.delhivery.com"
  ),
  TripService(
      "https://orion-trip-api.delhivery.com",
      "https://orion-trip-api-dev.delhivery.com"
  ),
  PaymentService(
      "https://orion-payment-api.delhivery.com",
      "https://orion-payment-api-dev.delhivery.com"
  ),
  CityService(
      "https://orion-city-api.delhivery.com",
      "https://orion-city-api-dev.delhivery.com"
  ),
  WarehouseService(
      "https://orion-warehouse-api.delhivery.com",
      "https://orion-warehouse-api-dev.delhivery.com"
  ),
  WalletService(
      "https://orion-wallet-api.delhivery.com",
      "https://orion-wallet-api-dev.delhivery.com"
  ),
  ImageService(
      "https://51l1p3gsd7.execute-api.ap-southeast-1.amazonaws.com/prod/",
      "https://e4l81arqid.execute-api.ap-southeast-1.amazonaws.com/poc/"
  ),
  FuelService(
      "https://tryyippya1.execute-api.ap-southeast-1.amazonaws.com/prod/",
      "https://tryyippya1.execute-api.ap-southeast-1.amazonaws.com/dev/"
  );

  /**
   * Get url based on logic
   */
  fun url() =
    when (BuildConfig.FLAVOR) {
      "development" -> dev
      else -> prod
    }
}