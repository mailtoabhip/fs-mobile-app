package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.OMCRequest
import com.delhivery.axle.api.service.OMCService
import com.delhivery.axle.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OMCRepository @Inject constructor(
    private val omcService: OMCService
) : BaseRepository(){

    fun omcCard(omcRequest: OMCRequest) = omcService.omcCard(omcRequest).convertResponse()
}