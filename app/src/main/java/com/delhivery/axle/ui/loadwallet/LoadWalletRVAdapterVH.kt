package com.delhivery.axle.ui.loadwallet

import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ItemWalletHistoryBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Base view holder for wallet history adapter
 */
abstract class BaseLoadWalletRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseLoadWalletRVAdapterItem<*>>(
    binding: B
) : BaseViewHolder<B>(binding) {

    abstract fun bind(
        item: IT,
        _interface: LoadWalletRVAdapterInterface
    )
}

/**
 * Wallet history item view holder
 */
class WalletHistoryItemVH(binding: ItemWalletHistoryBinding) :
    BaseLoadWalletRVAdapterViewHolder<ItemWalletHistoryBinding, WalletHistoryItem>(binding) {

    override fun bind(
        item: WalletHistoryItem,
        _interface: LoadWalletRVAdapterInterface
    ) {
        binding.item = item.data
        // Reset to default (non-loading) state
        binding.iconRefresh.visibility = android.view.View.VISIBLE
        binding.progressRefresh.visibility = android.view.View.GONE
        binding.textRefreshLabel.text = binding.root.context.getString(com.delhivery.axle.R.string.label_refresh_status)
        binding.btnRefreshStatus.isClickable = true
        binding.btnRefreshStatus.setOnClickListener {
            // Show inline loading state
            binding.iconRefresh.visibility = android.view.View.GONE
            binding.progressRefresh.visibility = android.view.View.VISIBLE
            binding.textRefreshLabel.text = "Loading ..."
            binding.btnRefreshStatus.isClickable = false
            _interface.handleAction(LoadWalletAction_RefreshStatus, item)
        }
        binding.cardContent.setOnClickListener {
            _interface.handleAction(LoadWalletAction_ViewDetails, item)
        }
        binding.executePendingBindings()
    }
}
