package com.delhivery.axle.ui.home.fragments.loads

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.loads.HomeLoadsFilterAction
import com.delhivery.axle.data.home.loads.HomeLoadsInfoAction_EditRoute
import com.delhivery.axle.data.home.loads.HomeLoadsInfoAction_Search
import com.delhivery.axle.data.home.loads.HomeLoadsVehicleFilterAction
import com.delhivery.axle.databinding.ViewHomeLoadsFilterItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsInfoItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsMoreInfoItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsSearchItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

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
 * Loads filter view holder
 */
internal class HomeLoadsFilterItemVH(binding: ViewHomeLoadsFilterItemBinding) :
    BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsFilterItemBinding, HomeLoadsFilterItem>(binding) {
  override fun bind(
    item: HomeLoadsFilterItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    if (item.data.actionLabel) {
      binding.toggleRemovedMark.visibility = View.VISIBLE
    } else {
      binding.toggleRemovedMark.visibility = View.GONE
    }
    binding.llExpressToggle.clickToAction(HomeLoadsFilterAction, item, _interface)
    binding.llVehicleFilter.clickToAction(HomeLoadsVehicleFilterAction, item, _interface)
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
    val searchString = SpannableString(item.data.searchString)
    searchString.setSpan(
        colorSpan, searchString.length - 12,
        searchString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    binding.textSearch.text = searchString
    binding.textSearch.clickToAction(HomeLoadsInfoAction_Search, item, _interface)
  }
}


/**
 * More Info item view holder
 */
internal class HomeLoadsMoreInfoItemVH(binding: ViewHomeLoadsMoreInfoItemBinding) :
  BaseHomeLoadsRVAdapterViewHolder<ViewHomeLoadsMoreInfoItemBinding, HomeLoadsMoreInfoItem>(
    binding
  ) {
  override fun bind(
    item: HomeLoadsMoreInfoItem,
    _interface: HomeLoadsRVAdapterInterface
  ) {
    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.status_active))

    val editRouteString: Spannable = SpannableString(item.data.editRouteString)
    editRouteString.setSpan(
      colorSpan, editRouteString.length - 6,
      editRouteString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    binding.textEditRoute.text = editRouteString
    binding.textEditRoute.clickToAction(HomeLoadsInfoAction_EditRoute, item, _interface)
  }
}