package com.delhivery.axle.ui.kyc.address

import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class CommunicationAddressViewModel@Inject constructor(

) :
BaseViewModel() {

    var flatAddress=""
    var areaAddress=""
    var pincodeAddress=""
    var cityAddress=""
    var documentProof = ""

    var currentStep = ""

}