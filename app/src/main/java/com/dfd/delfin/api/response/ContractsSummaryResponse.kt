package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class ContractsSummaryResponse(
  @SerializedName("contract_counts") val contractsCount: ContractsCount
)


data class ContractsCount(
  @SerializedName("all") val all: List<ContractsCountItem>,
  @SerializedName("active") val active: List<ContractsCountItem>
)


data class ContractsCountItem(
  @SerializedName("key") val key: String?,
  @SerializedName("doc_count") val count: Int?=0
)
