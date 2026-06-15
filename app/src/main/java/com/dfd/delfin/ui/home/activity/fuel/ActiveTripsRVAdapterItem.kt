package com.dfd.delfin.ui.home.activity.fuel

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.data.transactions.TransactionProgressItemData
import com.dfd.delfin.data.transactions.TransactionTimeOutItemData
import com.dfd.delfin.data.transactions.TransactionWarningItemData
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Progress
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Timeout
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Trip
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsRVAdapterItemType.Warning

/**
 * RV item type for [ActiveTripsRVAdapter]
 */
enum class ActiveTripsRVAdapterItemType(val typeId: Int) {
  Trip(0),
  Progress(2),
  Warning(3),
  Timeout(4);

  companion object {
    /**
     * Get [ActiveTripsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { typeId == it.typeId }
  }
}

/**
 * Base fuel trips type adapter item
 */
abstract class BaseActiveTripsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: ActiveTripsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Trip item
 */
class ActiveTripFuelDataItem(data: HomeTripsItemData) :
    BaseActiveTripsRVAdapterItem<HomeTripsItemData>(Trip, data)

/**
 * Inline progress item
 */
class ActiveTripProgressItem(data: TransactionProgressItemData = TransactionProgressItemData()) :
    BaseActiveTripsRVAdapterItem<TransactionProgressItemData>(Progress, data)

/**
 * Warning/action item
 */
class ActiveTripWarningItem(data: TransactionWarningItemData) :
    BaseActiveTripsRVAdapterItem<TransactionWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class ActiveTripTimeoutItem(data: TransactionTimeOutItemData) :
    BaseActiveTripsRVAdapterItem<TransactionTimeOutItemData>(Timeout, data)