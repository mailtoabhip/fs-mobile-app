package com.delhivery.axle.repository

import com.auth0.android.jwt.JWT
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.api.FuelService
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wallet Repository
 * -- Manage interaction with wallet service
 *
 */
@Singleton
class FuelRepository @Inject constructor(
  private val fuelService: FuelService,
  private val userPrefs: UserPrefs
) : BaseRepository() {

  private fun walletId() =
    when (BuildConfig.FLAVOR) {
      "development" -> "wallet::wallet::fcb31360-7ae4-11e9-9d32-0223f692f646"
      else -> {
        val idSuffix = JWT(userPrefs.jwtToken!!).claims["sub"]?.asString()!!
        "wallet::wallet::${idSuffix.substring(11)}"
      }
    }

  /**
   * Fetch active fuel cards
   */
  fun fetchActiveFuelCards() = fuelService.fetchActiveFuelCards()
}