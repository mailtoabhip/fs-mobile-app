package com.delhivery.axle.ui.home.fragments.pod

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.bids.HomeBidsSearchAction_Search
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Dispactched
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Epod
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Physical
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomePodsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewPodItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Base Home Pod RV adapter view holder
 */
abstract class BaseHomePodsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomePodRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomePodRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: HomePodRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: HomePodRVAdapterInterface
  ) = post { _interface.handleAction(actionId, position, item) }
}

/**
 * Pod Header item view holder
 */
internal class HomePodHeaderItemVH(binding: ViewHomePodsHeaderItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewHomePodsHeaderItemBinding, HomePodHeaderItem>(binding) {
  override fun bind(
    item: HomePodHeaderItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.viewEpod.clickToAction(HomePodHeaderAction_Epod, item, adapterPosition, _interface)
    binding.viewPhysicalPod.clickToAction(
        HomePodHeaderAction_Physical, item, adapterPosition, _interface
    )
    binding.viewDispatched.clickToAction(
        HomePodHeaderAction_Dispactched, item, adapterPosition, _interface
    )
  }
}

/**
 * Pod item view holder
 */
internal class HomePodSearchItemVH(binding: ViewHomeSearchItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewHomeSearchItemBinding, HomePodSearchItem>(binding) {
  override fun bind(
    item: HomePodSearchItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.editQuery.hint = "Origin / Destination"
    binding.editQuery.clickToAction(HomeBidsSearchAction_Search, item, adapterPosition, _interface)
  }
}

/**
 * Pod child item view holder
 */
class HomePodItemVH(binding: ViewPodItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewPodItemBinding, HomePodTripItem>(binding) {
  override fun bind(
    item: HomePodTripItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.trip = item.data
    binding.root.clickToAction(
        HomeTripsRequestAction_ViewDetails, item, adapterPosition, _interface
    )

    binding.btnEpod.clickToAction(
        HomeTripsRequestAction_UploadEpod, item, adapterPosition, _interface
    )

    binding.btnCourier.clickToAction(
        HomeTripsRequestAction_UploadTracking, item, adapterPosition, _interface
    )

    binding.containerSelector.clickToAction(
        HomeTripsRequestAction_UploadTracking, item, adapterPosition, _interface
    )
  }
}

/**
 * Pod warning item view holder
 */
internal class HomePodWarningItemVH(binding: ViewWarningItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewWarningItemBinding, HomePodWarningItem>(binding) {
  override fun bind(
    item: HomePodWarningItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
  }
}

/**
 * Pod timeout view holder
 */
internal class HomePodTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewTimeOutItemBinding, HomePodTimeoutItem>(binding) {
  override fun bind(
    item: HomePodTimeoutItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
  }
}

/**
 * Progress inline viewholder
 */
internal class HomePodProgressItemVH(binding: ViewHomeBidsProgressItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewHomeBidsProgressItemBinding, HomePodProgressItem>(
        binding
    ) {
  override fun bind(
    item: HomePodProgressItem,
    _interface: HomePodRVAdapterInterface
  ) {

  }
}