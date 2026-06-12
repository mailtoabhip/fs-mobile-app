package com.dfd.delfin.ui.loadwallet

import com.dfd.delfin.data.BaseKeyTypeModel

/**
 * RV item type for [LoadWalletRVAdapter]
 */
enum class LoadWalletRVAdapterItemType(val typeId: Int) {
    HistoryItem(0);

    companion object {
        fun byTypeId(typeId: Int) = values().firstOrNull { typeId == it.typeId }
    }
}

/**
 * Base adapter item for wallet history
 */
abstract class BaseLoadWalletRVAdapterItem<D : BaseKeyTypeModel<String>>(
    val type: LoadWalletRVAdapterItemType,
    val data: D
) : BaseKeyTypeModel<String>() {
    override fun key() = data.key()
}

/**
 * Wallet history item
 */
class WalletHistoryItem(data: WalletHistoryItemData, val isRefreshing: Boolean = false) :
    BaseLoadWalletRVAdapterItem<WalletHistoryItemData>(LoadWalletRVAdapterItemType.HistoryItem, data)
