package com.delhivery.axle.ui.fastag.fastag_details

data class TransactionItem(
    val id: String?,
    val tollName: String?,
    val timestamp: String?,
    val amount: Double?,
    val tollPlazaId: String? = null,
    var isSelected: Boolean? = false
)
