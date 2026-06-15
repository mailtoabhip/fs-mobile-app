package com.dfd.delfin.ui.serviceConfigListing

data class ServiceConfigurationModel(
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val status: ServiceStatus? = null
)

data class ServiceStatus(
    val statusText: String,
    val statusDescription: String,
    val statusType: StatusType = StatusType.IN_PROGRESS
)

enum class StatusType {
    IN_PROGRESS,
    COMPLETED,
    PENDING
}
