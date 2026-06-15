package com.dfd.delfin.ui.profile.kycdetails.fragments

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.doc.DocDetailData
import com.dfd.delfin.data.doc.DocProgressItemData
import com.dfd.delfin.data.doc.DocTimeOutItemData
import com.dfd.delfin.data.doc.DocWarningItemData

/**
 * RV item type for [DocRVAdapter]
 */
enum class DocRVAdapterItemType(val typeId: Int) {
  DocItem(0),
  Warning(1),
  Progress(2),
  Timeout(3);

  companion object {
    /**
     * Get [DocRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home bids type adapter item
 */
abstract class BaseDocRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: DocRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}


/**
 * Gst item
 */
class DocDataItem(data: DocDetailData) :
    BaseDocRVAdapterItem<DocDetailData>(DocRVAdapterItemType.DocItem, data)

/**
 * Warning/action item
 */
class DocWarningItem(data: DocWarningItemData) :
    BaseDocRVAdapterItem<DocWarningItemData>(DocRVAdapterItemType.Warning, data)

/**
 * Timeout item
 */
class GstTimeoutItem(data: DocTimeOutItemData) :
    BaseDocRVAdapterItem<DocTimeOutItemData>(DocRVAdapterItemType.Timeout, data)

/**
 * Inline progress item
 */
class DocProgressItem(data: DocProgressItemData = DocProgressItemData()) :
    BaseDocRVAdapterItem<DocProgressItemData>(DocRVAdapterItemType.Progress, data)