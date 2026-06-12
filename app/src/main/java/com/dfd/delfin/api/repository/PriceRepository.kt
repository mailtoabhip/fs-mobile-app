package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.OfferRequest
import com.dfd.delfin.api.request.PriceDetailRequest
import com.dfd.delfin.api.request.UpdatePriceRequest
import com.dfd.delfin.api.service.PriceService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles price service
 */
@Singleton
class PriceRepository @Inject constructor(
  private val priceService: PriceService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {


  fun shareRate(request: UpdatePriceRequest) = priceService.updatePricingData(request).convertResponse()

  fun getUpdateFlag() = priceService.getOffersFlag().convertResponse()

  fun getOffers() = priceService.getOffers(OfferRequest()).convertResponse()

  fun getPricingData(request: PriceDetailRequest) = priceService.getPricingData(request).convertResponse()

}