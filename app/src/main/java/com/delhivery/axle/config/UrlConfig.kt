package com.delhivery.axle.config

import com.delhivery.axle.BuildConfig

/**
 * URL Config for all services
 */
enum class UrlConfig(
  private val prod: String,
  private val dev: String,
  private val uat: String
) {
  UMS(
      "https://api-ums.delhivery.com",
      "https://api-stage-ums.delhivery.com",
      "https://api-stage-ums.delhivery.com"
  ),
   TruckService(
     "https://orion-contract-api.delhivery.com/",
     "https://orion-contract-api-dev.delhivery.com/",
     "https://orion-contract-api-uat.delhivery.com/"
   ),
  BidService(
      "https://orion-bid-api.delhivery.com",
      "https://orion-bid-api-dev.delhivery.com",
      "https://orion-bid-api-uat.delhivery.com"
  ),
  UserService(
      "https://orion-user-api.delhivery.com",
      "https://orion-user-api-dev.delhivery.com",
      "https://orion-user-api-uat.delhivery.com"
  ),
  TransactionService(
      "https://orion-transaction-api.delhivery.com",
      "https://orion-transaction-api-dev.delhivery.com",
      "http://orion-transaction-api-uat.delhivery.com"
  ),
  TripService(
      "https://orion-trip-api.delhivery.com",
      "https://orion-trip-api-dev.delhivery.com",
      "http://orion-trip-api-uat.delhivery.com"
  ),
  PaymentService(
      "https://orion-payment-api.delhivery.com",
      "https://orion-payment-api-dev.delhivery.com",
      "https://orion-payment-api-uat.delhivery.com"
  ),
  PayableService(
          "https://93n0m304c4.execute-api.ap-southeast-1.amazonaws.com/prod/",
          "https://orion-payable-dev.delhivery.com",
          "https://orion-payable-uat.delhivery.com"
  ),
  CityService(
      "https://orion-city-api.delhivery.com",
      "https://orion-city-api-dev.delhivery.com",
      "https://orion-city-api-uat.delhivery.com"
  ),
  WarehouseService(
      "https://orion-warehouse-api.delhivery.com",
      "https://orion-warehouse-api-dev.delhivery.com",
      "https://orion-warehouse-api-uat.delhivery.com"
  ),
  WalletService(
      "https://orion-wallet-api.delhivery.com",
      "https://orion-wallet-api-dev.delhivery.com",
      "https://orion-wallet-api-uat.delhivery.com"
  ),
  ImageService(
      "https://51l1p3gsd7.execute-api.ap-southeast-1.amazonaws.com/prod/",
      "https://e4l81arqid.execute-api.ap-southeast-1.amazonaws.com/poc/",
      "https://szrwunpnp9.execute-api.us-east-1.amazonaws.com/default/"
  ),
  NotificationService(
      "https://orion-notification-api.delhivery.com",
      "https://orion-notification-api-dev.delhivery.com",
      "https://orion-notification-api-uat.delhivery.com"
  ),
  FuelService(
      "https://orion-iocl-api.delhivery.com",
      "https://orion-iocl-api.dev.delhivery.com",
      "https://orion-iocl-api.uat.delhivery.com"
  ),
  DashboardUrl(
      "https://orion.delhivery.com",
      "https://orion-dev.delhivery.com",
      "https://orion-uat.delhivery.com"
  ),
  LoadCycleService(
      "https://orion-load-fullcycle-api.delhivery.com",
      "https://orion-load-fullcycle-api-dev.delhivery.com",
      "https://orion-load-fullcycle-api-uat.delhivery.com"
  ),
  ExpenseService(
      "https://orion-expense-api.delhivery.com",
      "https://orion-expense-api.dev.delhivery.com",
      "https://orion-expense-api-uat.delhivery.com"
  ),
  UtilityService(
      "https://orion-utility-api.delhivery.com",
      "https://orion-utility-api-dev.delhivery.com",
      "https://orion-utility-api-uat.delhivery.com"
  ),
  OMCService(
      "https://orion-omc-channel.delhivery.com",
      "https://orion-omc-dev.delhivery.com",
      "https://orion-omc-uat.delhivery.com"
  ),
  InventoryService(
      "https://orion-inventory-api-v2.delhivery.com",
      "https://zxfddco9gg.execute-api.ap-southeast-1.amazonaws.com/dev/",
      "https://orion-uat-inventory.delhivery.com"

  ),
  LoadboardService(
          "https://orion-user-loadboard.delhivery.com",
          "https://orion-user-onboarding-api-dev.delhivery.com",
          "https://orion-user-onboarding-api-uat.delhivery.com"
  ),
  PriceService(
          "https://orion-contract-api.delhivery.com/",
          "https://orion-contract-api-dev.delhivery.com/",
          "https://orion-contract-api-uat.delhivery.com/"
  ),
  RecommendationService(
          "https://orion-recommendation-api.delhivery.com",
          "https://orion-recommendation-api-dev.delhivery.com",
          "https://orion-recommendation-api-uat.delhivery.com"
  ),
  AppID(
  "83", "371", "371"
  );

  /**
   * Get url based on logic
   */
  fun url() =
    when (BuildConfig.FLAVOR) {
      "development" -> dev
      "uat" -> uat
      else -> prod
    }
}