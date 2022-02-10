package com.delhivery.axle.ui.kyc.address

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.address.AddressDetailData
import com.delhivery.axle.data.address.AddressTimeOutItemData
import com.delhivery.axle.data.address.AddressWarningItemData


enum class AddressRVAdapterItemType(val typeId: Int) {
    AddressItem(0),
    Warning(1),
    Progress(2),
    Timeout(3);

    companion object {
        /**
         * Get [AddressRVAdapterItemType] by typeId
         */
        fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
    }
}

/**
 * Base Home bids type adapter item
 */
abstract class BaseAddressRVAdapterItem<D : BaseKeyTypeModel<String>>(
    val type: AddressRVAdapterItemType,
    val data: D
) : BaseKeyTypeModel<String>() {
    override fun key() = data.key()
}


/**
 * Gst item
 */
class AddressDataItem(data: AddressDetailData) :
    BaseAddressRVAdapterItem<AddressDetailData>(AddressRVAdapterItemType.AddressItem, data)

/**
 * Warning/action item
 */
class AddressWarningItem(data: AddressWarningItemData) :
    BaseAddressRVAdapterItem<AddressWarningItemData>(AddressRVAdapterItemType.Warning, data)

/**
 * Timeout item
 */
class AddressTimeoutItem(data: AddressTimeOutItemData) :
    BaseAddressRVAdapterItem<AddressTimeOutItemData>(AddressRVAdapterItemType.Timeout, data)

/**
 * Inline progress item
 */
//class AddressProgressItem(data: AddressProgressItemData = AddressProgressItemData()) :
//    BaseAddressRVAdapterItem<AddressProgressItemData>(AddressRVAdapterItemType.Progress, data)