package com.delhivery.axle.ui.fastag

data class TransactionItem(
    val id: String?,
    val tollName: String?,
    val timestamp: String?,
    val amount: Double?,
    var isSelected: Boolean? = false
)
