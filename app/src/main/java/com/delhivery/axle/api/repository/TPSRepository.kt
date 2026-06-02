package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.api.service.TPSService
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.convertTPSResponse
import com.delhivery.axle.utils.prefs.UserPrefs
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