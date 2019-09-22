package com.delhivery.axle.ui.home.activity.transactionlist

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Enum to hold various transaction types
 *
 **
 */
enum class TransactionType(
  val typeId: Int,
  val typeText: String
) {
  Unknown(-1, "NA"),
  AdvancePending(0, "Advance Pending"),
  InTransit(1, "InTransit"),
  BalancePending(2, "Balance Pending"),
  Completed(3, "Completed");

  companion object {

    /**
     * Get [TransactionType] by type id
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { it.typeId == typeId } ?: Unknown

  }
}