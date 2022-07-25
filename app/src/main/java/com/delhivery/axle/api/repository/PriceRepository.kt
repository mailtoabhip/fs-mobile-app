package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.OfferRequest
import com.delhivery.axle.api.request.PriceDetailRequest
import com.delhivery.axle.api.request.UpdatePriceRequest
import com.delhivery.axle.api.service.LoadCycleService
import com.delhivery.axle.api.service.PriceService
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles price service
 */
@Singleton
class PriceRepository @Inject constructor(
  private val priceService: PriceService
) : BaseRepository() {


  fun shareRate(request: UpdatePriceRequest) = priceService.updatePricingData(request).convertResponse()

  fun getUpdateFlag() = priceService.getOffersFlag().convertResponse()

  fun getOffers() = priceService.getOffers(OfferRequest()).convertResponse()

  fun getPricingData(request: PriceDetailRequest) = priceService.getPricingData(request).convertResponse()

}