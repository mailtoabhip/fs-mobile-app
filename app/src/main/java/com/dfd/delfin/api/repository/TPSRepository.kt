package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.UpdateVehicleDetailsRequest
import com.dfd.delfin.api.service.TPSService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertTPSResponse
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class TPSRepository@Inject constructor(
    private val tpsService: TPSService,
    private val userPrefs: UserPrefs,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

    fun fetchPlacementTransactions() =
        tpsService.placementLoads(
            userPrefs.parentId).
        convertTPSResponse()

    fun getFacilityAddress(centerCode:String) =
        tpsService.getFacilityAddress(
            centerCode).
        convertTPSResponse()

    fun updateVehicleDetails(updateVehicleDetailsRequest: UpdateVehicleDetailsRequest) =
        tpsService.updateVehicleDetails(
            updateVehicleDetailsRequest).
        convertTPSResponse()

    /**
     * Placement details
     */
    fun getPlacementDetails(placementType: String, transactionId: String?=null, contractCode: String?=null) =
        tpsService.getPlacementDetails(placementType, transactionId, contractCode).convertTPSResponse()

}