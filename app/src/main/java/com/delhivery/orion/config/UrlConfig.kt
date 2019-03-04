package com.delhivery.orion.config

import com.delhivery.orion.BuildConfig

/**
 * URL Config for all services
 */
enum class UrlConfig(
  private val prod: String,
  private val dev: String
) {
  UMS("https://api-ums.delhivery.com", "https://api-stage-ums.delhivery.com");

  /**
   * Get url based on logic
   */
  fun url() = if (BuildConfig.DEBUG) dev else prod
}