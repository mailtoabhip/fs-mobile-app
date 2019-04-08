package com.delhivery.orion.repository

import com.delhivery.orion.api.BidService
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BidsRepository @Inject constructor(
  private val bidService: BidService
) : BaseRepository() {

  /**
   * Get user bids count, my bids and confirm bids
   */
  fun userBidsCount() = Single.just(Pair(5, 7))
}