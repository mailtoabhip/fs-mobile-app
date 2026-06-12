package com.dfd.delfin.ui.fastag.issuance

data class VehicleClassItem(
    val classId: String,
    val className: String,
    val weightRange: String,
    val vehicleTypes: List<String>,
    val indicatorColor: Int,
    var quantity: Int = 0,
    var selected: Boolean = false
)
