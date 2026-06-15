package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.OMCRequest
import com.dfd.delfin.api.service.OMCService
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OMCRepository @Inject constructor(
    private val omcService: OMCService,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger){

    fun omcCard(omcRequest: OMCRequest) = omcService.omcCard(omcRequest).convertResponse()
}