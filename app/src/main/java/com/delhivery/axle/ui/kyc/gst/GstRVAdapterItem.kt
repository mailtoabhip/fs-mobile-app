package com.delhivery.axle.ui.kyc.gst

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.gst.GstDetailData
import com.delhivery.axle.data.gst.GstProgressItemData
import com.delhivery.axle.data.gst.GstTimeOutItemData
import com.delhivery.axle.data.gst.GstWarningItemData
import com.delhivery.axle.ui.accountsetup.AccountSetupFragmentActionType

/**
 * RV item type for [GstRVAdapter]
 */
enum class GstRVAdapterItemType(val typeId: Int) {
  GstItem(0),
  Warning(1),
  Progress(2),
  Timeout(3);

  companion object {
    /**
     * Get [GstRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home bids type adapter item
 */
abstract class BaseGstRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: GstRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}


/**
 * Gst item
 */
class GstDataItem(data: GstDetailData) :
    BaseGstRVAdapterItem<GstDetailData>(GstRVAdapterItemType.GstItem, data)

/**
 * Warning/action item
 */
class GstWarningItem(data: GstWarningItemData) :
    BaseGstRVAdapterItem<GstWarningItemData>(GstRVAdapterItemType.Warning, data)

/**
 * Timeout item
 */
class GstTimeoutItem(data: GstTimeOutItemData) :
    BaseGstRVAdapterItem<GstTimeOutItemData>(GstRVAdapterItemType.Timeout, data)

/**
 * Inline progress item
 */
class GstProgressItem(data: GstProgressItemData = GstProgressItemData()) :
    BaseGstRVAdapterItem<GstProgressItemData>(GstRVAdapterItemType.Progress, data)