package com.delhivery.axle.ui.tripdetails

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.trips.HomeTripsTimeOutItemData
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryDetailItemData
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemData
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryProgressItemData
import com.delhivery.axle.ui.tripdetails.TripPaymentSummaryRVAdapterItemType.Detail
import com.delhivery.axle.ui.tripdetails.TripPaymentSummaryRVAdapterItemType.Progress
import com.delhivery.axle.ui.tripdetails.TripPaymentSummaryRVAdapterItemType.Summary
import com.delhivery.axle.ui.tripdetails.TripPaymentSummaryRVAdapterItemType.Timeout

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

enum class TripPaymentSummaryRVAdapterItemType(val typeId: Int) {

  Summary(0),
  Detail(1),
  Progress(2),
  Timeout(3);

  companion object{
    fun byTypeId(typeId: Int) = values().filter { typeId === it.typeId }.firstOrNull()
  }
}

abstract class BaseTripPaymentSummaryRVAdapterItem<D: BaseKeyTypeModel<String>>(
  val type: TripPaymentSummaryRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>(){
  override fun key() = data.key()
}

/**
 * Trip Summary Item
 * */
class TripSummaryItem(data: TripPaymentSummaryItemData) :
    BaseTripPaymentSummaryRVAdapterItem<TripPaymentSummaryItemData>(Summary, data)

/**
 * Trip Detail Item
 */
class TripSummaryDetailItem(data: TripPaymentSummaryDetailItemData) :
    BaseTripPaymentSummaryRVAdapterItem<TripPaymentSummaryDetailItemData>(Detail, data)

/**
 * Trip Progress Item
 */
class TripSummaryProgressItem(data: TripPaymentSummaryProgressItemData) :
    BaseTripPaymentSummaryRVAdapterItem<TripPaymentSummaryProgressItemData>(Progress, data)

/**
 * Trip Timeout Item
 */
class TripSummaryTimeoutItem(data: HomeTripsTimeOutItemData) :
    BaseTripPaymentSummaryRVAdapterItem<HomeTripsTimeOutItemData>(Timeout, data)

