package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for GET /api/v1/onboarding/services/{service_id}/requirements
 */
data class ServiceRequirementsResponse(
    @SerializedName("service_id") val serviceId: String,
    @SerializedName("provider_id") val providerId: String?,
    @SerializedName("config_version") val configVersion: Int,
    @SerializedName("onboarding_status") val onboardingStatus: String,
    @SerializedName("progress") val progress: OnboardingProgress,
    @SerializedName("sections") val sections: List<DocumentSection>
)

data class OnboardingProgress(
    @SerializedName("completed_documents") val completedDocuments: Int,
    @SerializedName("required_documents") val requiredDocuments: Int,
    @SerializedName("completion_percent") val completionPercent: Int
)

data class DocumentSection(
    @SerializedName("section") val section: String,
    @SerializedName("documents") val documents: List<DocumentRequirement>
)

data class DocumentRequirement(
    @SerializedName("document_type") val documentType: String,
    @SerializedName("label") val label: String,
    @SerializedName("sequence") val sequence: Int,
    @SerializedName("is_required") val isRequired: Boolean,
    @SerializedName("is_visible") val isVisible: Boolean,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("is_enabled") val isEnabled: Boolean,
    @SerializedName("depends_on") val dependsOn: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("value") val value: String? = null,
    @SerializedName("file_url") val fileUrl: String? = null,
    @SerializedName("reused") val reused: Boolean = false,
    @SerializedName("verification_mode") val verificationMode: List<String>? = null,
    @SerializedName("collection_mode") val collectionMode: List<String>? = null,
    @SerializedName("actions") val actions: DocumentActions? = null
)

data class DocumentActions(
    @SerializedName("can_edit") val canEdit: Boolean,
    @SerializedName("can_reupload") val canReupload: Boolean
)
