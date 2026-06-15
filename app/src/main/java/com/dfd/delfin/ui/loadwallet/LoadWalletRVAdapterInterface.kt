package com.dfd.delfin.ui.loadwallet

import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

/**
 * Adapter interface for [LoadWalletActivity]
 */
interface LoadWalletRVAdapterInterface : ItemClickListener<BaseLoadWalletRVAdapterItem<*>> {

    override fun onItemClicked(item: BaseLoadWalletRVAdapterItem<*>) {
        if (item.type == LoadWalletRVAdapterItemType.HistoryItem) {
            handleAction(LoadWalletAction_RefreshStatus, item)
        }
    }

    fun handleAction(
        actionId: String,
        item: BaseLoadWalletRVAdapterItem<*>
    )
}

const val LoadWalletAction_RefreshStatus = "refresh_status"
const val LoadWalletAction_ViewDetails = "view_details"
