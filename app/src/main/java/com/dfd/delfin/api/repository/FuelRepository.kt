package com.dfd.delfin.api.repository

import com.auth0.android.jwt.JWT
import com.dfd.delfin.api.service.FuelService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuel Repository
 * -- Manage interaction with fuel service
 *
 */
@Singleton
class FuelRepository @Inject constructor(
  private val fuelService: FuelService,
  private val userPrefs: UserPrefs,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  private fun walletId(): String =
//    when (BuildConfig.FLAVOR) {
//      "development" -> "wallet::wallet::fcb31360-7ae4-11e9-9d32-0223f692f646"
//      else ->
//    {
    "wallet::wallet::${JWT(userPrefs.jwtToken!!).claims["sub"]?.asString()!!.substring(11)}"
//      }

  /**
   * Fetch active fuel cards
   */
  fun fetchActiveFuelCards() = fuelService.fetchActiveFuelCards(walletId()).convertResponse()

  /**
   * Fetch fuel card by [tripId] [pan]
   */
  fun fetchFuelCard(
    tripId: String,
    pan: String
  ) = fuelService.fetchFuelCard(tripId, pan, walletId()).convertResponse()
}