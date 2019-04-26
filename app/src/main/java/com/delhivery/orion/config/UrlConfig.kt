package com.delhivery.orion.config

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
  );

  /**
   * Get url based on logic
   */
  fun url() = dev//if (BuildConfig.DEBUG) dev else prod
}