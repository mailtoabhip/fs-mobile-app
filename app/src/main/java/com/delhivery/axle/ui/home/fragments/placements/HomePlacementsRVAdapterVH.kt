package com.delhivery.axle.ui.home.fragments.placements

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.DynamicDrawableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.data.home.placements.HomePlacementsCallDriver
import com.delhivery.axle.data.home.placements.HomePlacementsFilterDelay
import com.delhivery.axle.data.home.placements.HomePlacementsFilterExpected
import com.delhivery.axle.data.home.placements.HomePlacementsFilterMissing
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewPlacementsDurationsBinding
import com.delhivery.axle.databinding.ViewPlacementsFiltersBinding
import com.delhivery.axle.databinding.ViewPlacementsNoDelayBinding
import com.delhivery.axle.databinding.ViewPlacementsTypeBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewVehiclePlacementBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.utils.StringUtils

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseHomePlacementsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomePlacementsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
    abstract fun bind(
        item: IT,
        _interface: HomePlacementsRVAdapterInterface
    )


    /**
     * Add on click listener for action
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        _interface: HomePlacementsRVAdapterInterface
    ) = setOnClickListener { action(actionId, item, _interface) }

    /**
     * Add on click listener for action with position
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        position: Int,
        _interface: HomePlacementsRVAdapterInterface
    ) = setOnClickListener { action(actionId, item, position, _interface) }

    /**
     * Post action to UI
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        _interface: HomePlacementsRVAdapterInterface
    ) = post { _interface.handleAction(actionId, item) }

    /**
     * Post action to UI with position
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        position: Int,
        _interface: HomePlacementsRVAdapterInterface
    ) = post { _interface.handleAction(actionId, item, position) }
}

/**
 * Bid request item view holder
 */

internal class HomeVehiclePlacementsRequestItemVH(binding:ViewVehiclePlacementBinding) :
    BaseHomePlacementsRVAdapterViewHolder<ViewVehiclePlacementBinding, HomeVehiclePlacementsRequestItem>(binding) {
    override fun bind(
        item: HomeVehiclePlacementsRequestItem,
        _interface: HomePlacementsRVAdapterInterface
    ) {
        binding.request = item.data
        binding.driverTruckDetails.request = item.data
        binding.originDestination.request = item.data
        binding.placementActions.request = item.data
        binding.placementActions.btnCall.clickToAction(HomePlacementsCallDriver, item, _interface)

        val spannable = SpannableStringBuilder()
        spannable.clearSpans()
// Origin text (black, bold)
        val origin = if(item.data.loadType== LoadTypes.intracityAdhoc.name || item.data.loadType == LoadTypes.intracityRegular.name) StringUtils.capitalize(item.data.originCenterName) else item.data.formattedOriginCity() ?: ""
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
        if (item.data.loadType == LoadTypes.orionFixed.name || item.data.loadType == LoadTypes.orionSpot.name || item.data.loadType == LoadTypes.ftlRegular.name || item.data.loadType == LoadTypes.ftlAdhoc.name) {
// Arrow as inline image
        val arrowDrawable =
            ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward_blue)
        arrowDrawable?.let { drawable ->
            val textSize = binding.originDestination.originDestinationText.textSize.toInt()
            drawable.setBounds(0, 0, textSize, textSize)

            val arrowStart = spannable.length
            spannable.append("\u200B") // zero-width space as placeholder
            spannable.setSpan(
                ImageSpan(drawable),
                arrowStart,
                arrowStart + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        spannable.append(" ")

// Destination text (black, bold)
        val destStart = spannable.length
        val destination = item.data.formattedDestinationCity() ?: ""
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

// Stops text (grey, normal)
        if (item.data.haltStops().isNotEmpty()) {
            spannable.append("  ")
            val stopStart = spannable.length
            spannable.append(item.data.haltStops())
            val stopEnd = spannable.length
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_grey)),
                stopStart,
                stopEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
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

        when(item.data.loadType){
            LoadTypes.ftlAdhoc.name-> {
                binding.placementLoadType.text =  "Delhivery Load"
                binding.placementLoadType.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))
                binding.placementLoadType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delhivery_load_flash,0,0,0)
                binding.placementLoadType.setBackgroundResource(R.drawable.bg_all_rounded_delhivery) }
            LoadTypes.ftlRegular.name-> {
                binding.placementLoadType.text =  "Delhivery Load"
                binding.placementLoadType.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))
                binding.placementLoadType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delhivery_load_flash,0,0,0)
                binding.placementLoadType.setBackgroundResource(R.drawable.bg_all_rounded_delhivery)}
            LoadTypes.intracityRegular.name->  {
                binding.placementLoadType.text ="Delhivery Contract"
                binding.placementLoadType.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))
                binding.placementLoadType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delhivery_load_flash,0,0,0)
                binding.placementLoadType.setBackgroundResource(R.drawable.bg_all_rounded_delhivery)}
            LoadTypes.intracityAdhoc.name-> {
                binding.placementLoadType.text ="Delhivery Load"
                binding.placementLoadType.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))
                binding.placementLoadType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delhivery_load_flash,0,0,0)
                binding.placementLoadType.setBackgroundResource(R.drawable.bg_all_rounded_delhivery)}
            LoadTypes.orionFixed.name->  {
                binding.placementLoadType.text ="Client Contract"
                binding.placementLoadType.setTextColor(ContextCompat.getColor(context, R.color.orange_v3))
                binding.placementLoadType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_client_placement,0,0,0)
                binding.placementLoadType.setBackgroundResource(R.drawable.bg_all_rounded_client)}
            LoadTypes.orionSpot.name->  {
                binding.placementLoadType.text ="Client Load"
                binding.placementLoadType.setTextColor(ContextCompat.getColor(context, R.color.orange_v3))
                binding.placementLoadType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_client_placement,0,0,0)
                binding.placementLoadType.setBackgroundResource(R.drawable.bg_all_rounded_client)}
            else -> {}
        }
        when(item.data.status){
            "Delayed"-> {
                binding.tvStatusDuration.background = null
                binding.tvStatusDuration.setTextColor(ContextCompat.getColor(context, R.color.light_orange_color))
                binding.tvStatusDuration.setPadding(
                    0,0,0,0
                )

            }
            "Marked-in"-> {
                binding.tvStatusDuration.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_rounded_marked_in)
                binding.tvStatusDuration.setTextColor(ContextCompat.getColor(context, R.color.bid_placed_green))
                val scale = context.resources.displayMetrics.density
                binding.tvStatusDuration.setPadding(
                    (8 * scale).toInt(),
                    (4 * scale).toInt(),
                    (8 * scale).toInt(),
                    (4 * scale).toInt()
                )

            }
            "Expected"-> {
                binding.tvStatusDuration.background = null
                binding.tvStatusDuration.setTextColor(ContextCompat.getColor(context, R.color.text_grey_v3))
                binding.tvStatusDuration.setPadding(
                    0,0,0,0
                )

            }

        }


    }
}


/**
 * Loads intracity filter view holder
 */
internal class HomePlacementsTypeItemVH(binding:ViewPlacementsTypeBinding) :
    BaseHomePlacementsRVAdapterViewHolder<ViewPlacementsTypeBinding, HomePlacementsTypeItem>(binding) {
    override fun bind(
        item: HomePlacementsTypeItem,
        _interface: HomePlacementsRVAdapterInterface
    ) {
        binding.request = item.data
    }
}

internal class HomePlacementsDurationItemVH(binding:ViewPlacementsDurationsBinding) :
    BaseHomePlacementsRVAdapterViewHolder<ViewPlacementsDurationsBinding, HomePlacementsDurationItem>(binding) {
    override fun bind(
        item: HomePlacementsDurationItem,
        _interface: HomePlacementsRVAdapterInterface
    ) {
        binding.durationText.text = item.data.duration
    }
}


internal class HomePlacementsFilterItemVH(binding:ViewPlacementsFiltersBinding) :
    BaseHomePlacementsRVAdapterViewHolder<ViewPlacementsFiltersBinding, HomePlacementsFilterItem>(binding) {
    override fun bind(
        item: HomePlacementsFilterItem,
        _interface: HomePlacementsRVAdapterInterface
    ) {
        binding.request = item.data
        when (item.data.filterType) {
            PlacementTypes.Delayed.name -> {
                binding.llDelay.isSelected = true
                binding.llMissing.isSelected = false
                binding.llExpected.isSelected = false
            }

            PlacementTypes.MissingDetails.name -> {
                binding.llDelay.isSelected = false
                binding.llMissing.isSelected = true
                binding.llExpected.isSelected = false
            }

            PlacementTypes.Expected.name -> {
                binding.llDelay.isSelected = false
                binding.llMissing.isSelected = false
                binding.llExpected.isSelected = true
            }
        }
        binding.llDelay.clickToAction(HomePlacementsFilterDelay, item, _interface)
        binding.llMissing.clickToAction(HomePlacementsFilterMissing, item, _interface)
        binding.llExpected.clickToAction(HomePlacementsFilterExpected, item, _interface)
    }
}

    internal class HomePlacementsNoDelayItemVH(binding: ViewPlacementsNoDelayBinding) :
        BaseHomePlacementsRVAdapterViewHolder<ViewPlacementsNoDelayBinding, HomePlacementsNoDelayItem>(
            binding
        ) {
        override fun bind(
            item: HomePlacementsNoDelayItem,
            _interface: HomePlacementsRVAdapterInterface
        ) {
            binding.request = item.data
            when(item.data.status){
                "No_Delay"->  binding.infoIcon.setImageResource(R.drawable.ic_no_delayed)
                "Missing"->  binding.infoIcon.setImageResource(R.drawable.ic_no_missing)
                "No_Pipeline"->  binding.infoIcon.setImageResource(R.drawable.ic_no_expected)
            }

        }
    }

    /**
     * Loads timeout view holder
     */
    internal class HomePlacementsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
        BaseHomePlacementsRVAdapterViewHolder<ViewTimeOutItemBinding, HomePlacementsTimeoutItem>(
            binding
        ) {
        override fun bind(
            item: HomePlacementsTimeoutItem,
            _interface: HomePlacementsRVAdapterInterface
        ) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, _interface)
        }
    }


    internal class HomePlacementsWarningItemVH(binding: ViewWarningItemBinding) :
        BaseHomePlacementsRVAdapterViewHolder<ViewWarningItemBinding, HomePlacementsWarningItem>(
            binding
        ) {
        override fun bind(
            item: HomePlacementsWarningItem,
            _interface: HomePlacementsRVAdapterInterface
        ) {
            binding.title = item.data.title
            binding.subTitle = item.data.subtitle
            binding.actionLabel = item.data.actionLabel
            binding.btnAction.clickToAction(item.data.actionId, item, _interface)
        }
    }

    internal class HomePlacementsProgressItemVH(binding: ViewHomeContractsProgressItemBinding) :
        BaseHomePlacementsRVAdapterViewHolder<ViewHomeContractsProgressItemBinding, HomePlacementsProgressItem>(
            binding
        ) {
        override fun bind(
            item: HomePlacementsProgressItem,
            _interface: HomePlacementsRVAdapterInterface
        ) {
        }
    }


