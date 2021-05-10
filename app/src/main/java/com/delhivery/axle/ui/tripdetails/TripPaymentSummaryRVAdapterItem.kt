package com.delhivery.axle.ui.tripdetails

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

enum class TripPaymentSummaryRVAdapterItemType(val typeId: Int) {

  ItemSummary(0),
  ItemDetail(1),
  Progress(2),
  Timeout(3);

  companion object{
    fun byTypeId(typeId: Int) = ConsolidatedPageRVAdapterItemType.values()
        .filter { typeId === it.typeId }.firstOrNull()
  }
}

abstract class BaseTripPaymentSummaryRVAdapterItem<D: BaseKeyTypeModel<String>>(
  val type: TripPaymentSummaryRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>(){
  override fun key() = data.key()
}

/**
 * Payment Summary Item
 * */


