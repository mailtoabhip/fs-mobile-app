package com.delhivery.axle.ui.home.activity.fuel

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.transactions.TransactionProgressItemData
import com.delhivery.axle.data.transactions.TransactionTimeOutItemData
import com.delhivery.axle.data.transactions.TransactionWarningItemData
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Trip
import com.delhivery.axle.ui.home.activity.fuel.TripsFuelRVAdapterItemType.Warning

enum class TripsFuelRVAdapterItemType(val typeId: Int) {
  Trip(0),
  Progress(2),
  Warning(3),
  Timeout(4);

  companion object {
    /**
     * Get [TripsFuelRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { typeId == it.typeId }
  }
}

/**
 * Base fuel trips type adapter item
 */
abstract class BaseTripsFuelRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: TripsFuelRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Trip item
 */
class TripsFuelDataItem(data: HomeTripsItemData) :
    BaseTripsFuelRVAdapterItem<HomeTripsItemData>(Trip, data)

/**
 * Inline progress item
 */
class TripsFuelProgressItem(data: TransactionProgressItemData = TransactionProgressItemData()) :
    BaseTripsFuelRVAdapterItem<TransactionProgressItemData>(Progress, data)

/**
 * Warning/action item
 */
class TransactionWarningItem(data: TransactionWarningItemData) :
    BaseTripsFuelRVAdapterItem<TransactionWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class TransactionTimeoutItem(data: TransactionTimeOutItemData) :
    BaseTripsFuelRVAdapterItem<TransactionTimeOutItemData>(Timeout, data)