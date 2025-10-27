package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class UpdateVehicleDetailsRequest(
    @SerializedName("vehicle_number") var vehicleNumber: String,
    @SerializedName("driver_name") var driverName: String,
    @SerializedName("driver_number") var driverNumber: String,
    @SerializedName("contract_type") var contractType: String?,
    @SerializedName("vehicle_type") var vehicleType: String?,
    @SerializedName("vehicle_dimension") var vehicleDimension: String,
    @SerializedName("transporter_supplier_id") var transporterSupplierId: String,
    @SerializedName("contract_id") var contractId: String?=null,
    @SerializedName("transporter_id") var transporterId: Int,
    @SerializedName("action") var action: String?,
    @SerializedName("reporting_time") var reportingTime: String?,
    @SerializedName("center_code") var centerCode:String?,
    @SerializedName("old_vehicle_number") var oldVehicleNumber: String?,
    @SerializedName("old_vehicle_id") var oldVehicleId: String?,
    @SerializedName("old_driver_name") var oldDriverName: String?,
    @SerializedName("old_driver_number") var oldDriverNumber: String?,
    @SerializedName("transaction_id") var transactionId: String?=null,

    )