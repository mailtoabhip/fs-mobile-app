package com.delhivery.axle.api.repository

import com.delhivery.axle.api.service.PayableService
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PayableRepository @Inject constructor(
        private val payableService: PayableService
): BaseRepository() {
    /**
     * Get Charges List
     */
    fun fetchChargesList(
            payload: JsonObject
    ) = payableService.fetchChargeList(payload).convertResponse()

    /**
     * Get DN List
     */
    fun fetchDNList(
            payload: JsonObject
    ) = payableService.fetchDNList(payload).convertResponse()

    fun fetchConsolidatedLedgerList(
            payload: JsonObject
    ) = payableService.fetchConsolidatedLedgerList(payload).convertResponse()
}