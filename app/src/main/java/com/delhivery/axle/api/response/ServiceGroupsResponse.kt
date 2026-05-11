package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for GET /api/v1/onboarding/service-groups
 */
data class ServiceGroupsResponse(
    @SerializedName("groups") val groups: List<ServiceGroup>
)

/**
 * Individual service group item.
 *
 * @param groupId Primary key identifier for the group
 * @param groupName UI label for the group
 * @param description Card subtitle text
 * @param displayOrder UI sorting order
 * @param icon Optional icon metadata for UI rendering
 * @param serviceCount Number of services in this group (useful for badges)
 */
data class ServiceGroup(
    @SerializedName("group_id") val groupId: String,
    @SerializedName("group_name") val groupName: String,
    @SerializedName("description") val description: String,
    @SerializedName("display_order") val displayOrder: Int,
    @SerializedName("icon") val icon: String?,
    @SerializedName("service_count") val serviceCount: Int
)
