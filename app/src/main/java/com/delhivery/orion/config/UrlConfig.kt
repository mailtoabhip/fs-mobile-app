package com.delhivery.orion.config

import com.delhivery.orion.BuildConfig

/**
 * URL Config for all services
 */
enum class UrlConfig(
  private val prod: String,
  private val dev: String
) {
  UMS("https://api-ums.delhivery.com", "https://api-stage-ums.delhivery.com"),
  BidService(
      "https://sbx6ojnl19.execute-api.ap-southeast-1.amazonaws.com/dev/",
      "https://sbx6ojnl19.execute-api.ap-southeast-1.amazonaws.com/dev/"
  ),
  OrionData(
      "https://orion-user-api-dev.delhivery.com/",
      "https://orion-user-api-dev.delhivery.com/"
  );

  /**
   * Get url based on logic
   */
  fun url() = if (BuildConfig.DEBUG) dev else prod
}