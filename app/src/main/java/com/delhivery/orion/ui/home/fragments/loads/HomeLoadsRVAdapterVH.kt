package com.delhivery.orion.ui.home.fragments.loads

import android.databinding.ViewDataBinding
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_AcceptBid
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.orion.data.home.loads.HomeLoadsInfoAction_EditRoute
import com.delhivery.orion.data.home.loads.HomeLoadsInfoAction_Search
import com.delhivery.orion.databinding.ViewHomeLoadsInfoItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsSearchItemBinding
import com.delhivery.orion.databinding.ViewTimeOutItemBinding
import com.delhivery.orion.databinding.ViewWarningItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseHomeLoadsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeLoadsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomeLoadsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: HomeLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: HomeLoadsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: HomeLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: HomeLoadsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item, position) }
}

/**
 * Bid request item view holder
 */
class HomeLoadsRequestItemVH(binding: ViewHomeLoadsRequestItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsRequestItemBinding, HomeLoadsRequestItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsRequestItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.request = item.data
    binding.btnAccept.clickToAction(
        HomeBidsRequestAction_AcceptBid, item, adapterPosition, _interface
    )
    binding.btnBid.clickToAction(HomeBidsRequestAction_PlaceBid, item, adapterPosition, _interface)
    binding.viewBidInfo.clickToAction(
        HomeBidsRequestAction_PlaceBid, item, adapterPosition, _interface
    )
  }
}

/**
 * Progress inline view holder
 */
internal class HomeLoadsProgressItemVH(binding: ViewHomeLoadsProgressItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsProgressItemBinding, HomeLoadsProgressItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsProgressItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
  }
}

/**
 * Search item view holder
 */
internal class HomeLoadsSearchItemVH(binding: ViewHomeLoadsSearchItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsSearchItemBinding, HomeLoadsSearchItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsSearchItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
  }
}

/**
 * Bids warning item view holder
 */
internal class HomeLoadsWarningItemVH(binding: ViewWarningItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewWarningItemBinding, HomeLoadsWarningItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsWarningItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Loads timeout view holder
 */
internal class HomeLoadsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewTimeOutItemBinding, HomeLoadsTimeoutItem>(binding) {
  override fun bind(
    item: HomeLoadsTimeoutItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Info item view holder
 */
internal class HomeLoadsInfoItemVH(binding: ViewHomeLoadsInfoItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsInfoItemBinding, HomeLoadsInfoItem>(
        binding
    ) {
  override fun bind(
    item: HomeLoadsInfoItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.status_active))
    val searchString: Spannable = SpannableString(item.data.searchString)
    searchString.setSpan(
        colorSpan, searchString.length - 13,
        searchString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    binding.textSearch.text = searchString
    binding.textSearch.clickToAction(HomeLoadsInfoAction_Search, item, _interface)

    val editRouteString: Spannable = SpannableString(item.data.editRouteString)
    editRouteString.setSpan(
        colorSpan, editRouteString.length - 6,
        editRouteString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    binding.textEditRoute.text = editRouteString
    binding.textEditRoute.clickToAction(HomeLoadsInfoAction_EditRoute, item, _interface)
  }
}