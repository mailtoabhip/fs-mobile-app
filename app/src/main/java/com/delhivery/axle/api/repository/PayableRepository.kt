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

    /**
     * Get Collection List
     */
    fun fetchCollectionList(
            payload: JsonObject
    ) = payableService.fetchCollectionList(payload).convertResponse()

    /**
     * Get Consolidated Ledger List
     */
    fun fetchConsolidatedLedgerList(
            payload: JsonObject
    ) = payableService.fetchConsolidatedLedgerList(payload).convertResponse()

    /**
     * Email Ledger
     */
    fun emailVendorLedger(
            payload: JsonObject
    ) = payableService.emailVendorLedger(payload).convertResponse()

    /**
     * Download Ledger
     */
    fun downloadVendorLedger(
            payload: JsonObject
    ) = payableService.downloadVendorLedger(payload).convertResponse()

    /**
     * List Invoices
     */
    fun listInvoices(
            tripId: String
    ) = payableService.listInvoices(tripId).convertResponse()

  /**
   * List dn recoveries
   */
  fun listDNRecoveries(
    payload: JsonObject
  ) = payableService.listDNRecoveries(payload).convertResponse()

  /**
   * List overpayment recoveries
   */
  fun listOverpaymentRecoveries(
    payload: JsonObject
  ) = payableService.listOverpaymentRecoveries(payload).convertResponse()

}


/* User consolidated pagination load limit */
const val UserSearchLimitConsolidatedAPI = 20