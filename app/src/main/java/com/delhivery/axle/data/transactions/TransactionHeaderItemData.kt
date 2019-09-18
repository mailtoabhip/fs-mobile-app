package com.delhivery.axle.data.transactions

import com.delhivery.axle.api.response.Summary
import com.delhivery.axle.data.BaseKeyTypeModel

data class TransactionHeaderItemData(
  val advancePending: Summary? = null,
  val balancePending: Summary? = null,
  val inTransit: Summary? = null,
  val completed: Summary? = null
) : BaseKeyTypeModel<String>() {
  override fun key() = TransactionHeaderItemDataKey
}

/* unique key for diff */
const val TransactionHeaderItemDataKey = "header"

/* actions */
const val TransactionHeaderAction_AdvancePending = "advance_pending"