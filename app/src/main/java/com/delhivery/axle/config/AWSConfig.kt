package com.delhivery.axle.config

import com.delhivery.axle.BuildConfig

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 *
 * Get AWS target and bucket
 *
 **
 */
enum class AWSConfig(
  private val prod: String,
  private val dev: String,
  private val uat: String
) {

  Target("orion-sts-prod", "orion-sts-dev", "orion-sts-dev"),
  Bucket("orion-service-prod", "orion-service", "orion-uat"),
  ServerRegion("ap-southeast-1", "ap-southeast-1", "us-east-1");

  fun value() =
    when (BuildConfig.FLAVOR) {
      "development" -> dev
      "uat" -> uat
      else -> prod
    }
}