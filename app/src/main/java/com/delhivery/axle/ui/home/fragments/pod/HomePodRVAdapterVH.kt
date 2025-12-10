package com.delhivery.axle.ui.home.fragments.pod

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Dispactched
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Epod
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Physical
import com.delhivery.axle.data.home.pod.HomePodSearchAction_Search
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ShowLRList
import com.delhivery.axle.data.home.trips.PODStatus
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomePodsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewNewPodItemBinding
import com.delhivery.axle.databinding.ViewPodItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.utils.StringUtils

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
    binding.viewEpod.clickToAction(HomePodHeaderAction_Epod, item, bindingAdapterPosition, _interface)
    binding.viewPhysicalPod.clickToAction(
        HomePodHeaderAction_Physical, item, bindingAdapterPosition, _interface
    )
    binding.viewDispatched.clickToAction(
        HomePodHeaderAction_Dispactched, item, bindingAdapterPosition, _interface
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
    binding.editQuery.hint = "Vehicle/LR Number"
    binding.editQuery.clickToAction(HomePodSearchAction_Search, item, bindingAdapterPosition, _interface)
  }
}

/**
 * Pod child item view holder
 */
class HomePodItemVH(binding: ViewNewPodItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewNewPodItemBinding, HomePodTripItem>(binding) {
  override fun bind(
    item: HomePodTripItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.trip = item.data
    // isHPODSection will be set by the adapter in bindVH
    val spannable = SpannableStringBuilder()
    spannable.clearSpans()
    val origin = item.data.formattedOriginCity() ?: ""
    val originStart = spannable.length
    spannable.append(origin)
    val originEnd = spannable.length
    spannable.setSpan(
      ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_black_v2)),
      originStart,
      originEnd,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannable.setSpan(
      StyleSpan(Typeface.BOLD),
      originStart,
      originEnd,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
// Arrow as inline image
      val arrowStart = spannable.length
      spannable.append(" ➔ ")
      spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        arrowStart,
        spannable.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )

// Destination text (black, bold)
      val destStart = spannable.length
      val destination = item.data.formattedDestinationCity()
      spannable.append(destination)
      val destEnd = spannable.length
      spannable.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_black_v2)),
        destStart,
        destEnd,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )
      spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        destStart,
        destEnd,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )

    binding.originDestination.originDestinationText.text = null
    binding.originDestination.originDestinationText.movementMethod = null
    binding.originDestination.originDestinationText.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

// Set the TextView safely
    binding.originDestination.originDestinationText.apply {
      text = spannable
      isClickable = false
      isFocusable = false
      linksClickable = false
      autoLinkMask = 0
      movementMethod = null
    }
    binding.podCardCl.clickToAction(
        HomeTripsRequestAction_ViewDetails, item, bindingAdapterPosition, _interface
    )
    val badge = binding.tvPodStatus
    when (item.data.podAction()) {
      PODStatus.REJECT -> {
        badge.text = "ePOD Rejected"
        badge.backgroundTintList = ColorStateList.valueOf("#FEEFEF".toColorInt())
        badge.setTextColor("#8E2720".toColorInt())
      }
        PODStatus.REVIEW -> {
        badge.text = "ePOD Under Review"
        badge.backgroundTintList = ColorStateList.valueOf("#F0F0F0".toColorInt())
        badge.setTextColor("#121A31".toColorInt())
      }
        PODStatus.VIEWPOD -> {
        badge.text = "ePOD Verified"
        badge.backgroundTintList = ColorStateList.valueOf("#ECFDF5".toColorInt())
        badge.setTextColor("#065F46".toColorInt())
      }
        PODStatus.UPLOAD -> {
        badge.text = "ePOD Pending"
        badge.backgroundTintList = ColorStateList.valueOf("#FFF7EB".toColorInt())
        badge.setTextColor(ColorStateList.valueOf("#935F07".toColorInt()))
      }
        null -> {
        badge.text = "ePOD Pending"
        badge.backgroundTintList = ColorStateList.valueOf("#FFF7EB".toColorInt())
        badge.setTextColor(ColorStateList.valueOf("#935F07".toColorInt()))
      }

    }
    binding.podActions.btnUploadEpod.clickToAction(
      HomeTripsRequestAction_UploadEpod, item, bindingAdapterPosition, _interface
    )
    binding.podActions.updateDetails.clickToAction(
      HomeTripsRequestAction_UploadTracking, item, bindingAdapterPosition, _interface
    )
    // Add click listener for lrNumbers TextView if there are multiple LRs
    if (item.data.hasMultipleLRs()) {
      binding.podActions.lrDetailsLL.clickToAction(
        HomeTripsRequestAction_ShowLRList, item, bindingAdapterPosition, _interface
      )
    }
    // Add click listener for lrNumber TextView in vehicle loaded details if there are multiple LRs
    if (item.data.hasMultipleLRs()) {
      binding.vehicleLoadedDetails.lrNumberContainer.clickToAction(
        HomeTripsRequestAction_ShowLRList, item, bindingAdapterPosition, _interface
      )
    }
   /* binding.btnEpod.clickToAction(
        HomeTripsRequestAction_UploadEpod, item, bindingAdapterPosition, _interface
    )

    binding.btnCourier.clickToAction(
        HomeTripsRequestAction_UploadTracking, item, bindingAdapterPosition, _interface
    )

    binding.containerSelector.clickToAction(
        HomeTripsRequestAction_UploadTracking, item, bindingAdapterPosition, _interface
    )*/
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
    binding.btnAction.clickToAction(item.data.actionId, item, bindingAdapterPosition, _interface)
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
    binding.btnAction.clickToAction(item.data.actionId, item, bindingAdapterPosition, _interface)
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