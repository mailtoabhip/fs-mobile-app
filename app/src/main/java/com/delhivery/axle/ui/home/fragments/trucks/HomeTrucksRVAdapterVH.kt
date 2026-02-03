package com.delhivery.axle.ui.home.fragments.trucks

import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.data.home.loads.HomeLoadsInfoAction_EditRoute
import com.delhivery.axle.data.home.loads.HomeLoadsPriorityAction
import com.delhivery.axle.data.home.trucks.*
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.home.fragments.trips.BaseHomeTripsRVAdapterViewHolder
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsActivity

/**
 * Base Home trucks RV adapter view holder
 */
abstract class BaseHomeTrucksRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeTrucksRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
    abstract fun bind(
        item: IT,
        _interface: HomeTrucksRVAdapterInterface
    )

    companion object {
        var bannerShown = false
    }

    /**
     * Add on click listener for action
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        _interface: HomeTrucksRVAdapterInterface
    ) = setOnClickListener { action(actionId, item, _interface) }

    /**
     * Add on click listener for action with position
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        position: Int,
        _interface: HomeTrucksRVAdapterInterface
    ) = setOnClickListener { action(actionId, item, position, _interface) }

    /**
     * Post action to UI
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        _interface: HomeTrucksRVAdapterInterface
    ) = post { _interface.handleAction(actionId, item) }

    /**
     * Post action to UI with position
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        position: Int,
        _interface: HomeTrucksRVAdapterInterface
    ) = post { _interface.handleAction(actionId, item, position) }
}

/**
 * Bid request item view holder
 */
class HomeTrucksRequestItemVH(binding: ViewHomeTrucksRequestItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewHomeTrucksRequestItemBinding, HomeTrucksRequestItem>(
        binding
    ) {
    override fun bind(
        item: HomeTrucksRequestItem,
        _interface: HomeTrucksRVAdapterInterface
    ) {
        binding.request = item.data
        binding.btnActivateTruck.clickToAction(HomeTrucksRequestAction_ActivateTruck , item, bindingAdapterPosition,_interface)

        binding.gotit.setOnClickListener { binding.rateMore.visibility = View.GONE }

        binding.shareRate.setOnClickListener {
            binding.rateMore.visibility = View.GONE
           _interface.callRewards()
        }
        
        // Refresh FASTag balance click listener
        binding.ivRefreshBalance.setOnClickListener {
            item.data.fastagTagId?.let { tagId ->
                _interface.refreshFastagBalance(tagId)
            }
        }
        
        // FASTag Details button click listener
        binding.btnFastagDetails.setOnClickListener {
            _interface.openFastagDetails(item.data)
        }
        
        // Buy FASTag button click listener
        binding.btnBuyFastagCard.clickToAction(HomeTrucksRequestAction_BuyFastag, item, _interface)
    }
}

/**
 * Progress inline view holder
 */
internal class HomeTrucksProgressItemVH(binding: ViewHomeTrucksProgressItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewHomeTrucksProgressItemBinding, HomeTrucksProgressItem>(
        binding
    ) {
    override fun bind(
        item: HomeTrucksProgressItem,
        _interface: HomeTrucksRVAdapterInterface
    ) {
    }
}

/**
 * Search item view holder
 */
internal class HomeTrucksSearchItemVH(binding: ViewHomeTrucksSearchItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewHomeTrucksSearchItemBinding, HomeTrucksSearchItem>(
        binding
    ) {
    override fun bind(
        item: HomeTrucksSearchItem,
        _interface: HomeTrucksRVAdapterInterface
    ) {
    }
}

/**
 * Bids warning item view holder
 */
internal class HomeTrucksWarningItemVH(binding: ViewWarningItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewWarningItemBinding, HomeTrucksWarningItem>(
        binding
    ) {
    override fun bind(
        item: HomeTrucksWarningItem,
        _interface: HomeTrucksRVAdapterInterface
    ) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, _interface)
    }
}

/**
 * Trucks filter view holder
 */
internal class HomeTrucksFilterItemVH(binding: ViewHomeTrucksFilterItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewHomeTrucksFilterItemBinding, HomeTrucksFilterItem>(binding) {
    override fun bind(
        item: HomeTrucksFilterItem,
        _interface: HomeTrucksRVAdapterInterface
    ) {
        binding.llVehicleFilter.clickToAction(HomeTrucksVehicleFilterAction, item , _interface)
        binding.llAvailabilityFilter.clickToAction(HomeTrucksAvailabilityFilterAction, item, _interface)
        binding.llTruckSizeFilter.clickToAction(HomeTrucksSizeFilterAction, item , _interface)
    }
}

/**
 * Trucks timeout view holder
 */
internal class HomeTrucksTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewTimeOutItemBinding, HomeTrucksTimeoutItem>(binding) {
    override fun bind(
        item: HomeTrucksTimeoutItem,
        _interface: HomeTrucksRVAdapterInterface
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
internal class HomeTrucksInfoItemVH(binding: ViewHomeTrucksInfoItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewHomeTrucksInfoItemBinding, HomeTrucksInfoItem>(
        binding
    ) {
    override fun bind(
        item: HomeTrucksInfoItem,
        _interface: HomeTrucksRVAdapterInterface
    ) {
        binding.trucksCount.text = "("+item.data.count.toString()+")"
        _interface.settotal(item.data.count)
    }
}


/**
 * More Info item view holder
 */
internal class HomeTrucksMoreInfoItemVH(binding: ViewHomeLoadsMoreInfoItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewHomeLoadsMoreInfoItemBinding, HomeTrucksMoreInfoItem>(
        binding
    ) {
    override fun bind(
        item: HomeTrucksMoreInfoItem,
        _interface: HomeTrucksRVAdapterInterface
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

/**
 *   Add Truck priority item view holder
 */
internal class HomeTruckPriorityItemVH(binding: ViewHomeLoadsTruckPriorityItemBinding) :
    BaseHomeTrucksRVAdapterViewHolder<ViewHomeLoadsTruckPriorityItemBinding, HomeTruckPriorityAccessItem>(
        binding
    ) {
    override fun bind(
        item: HomeTruckPriorityAccessItem,
        _interface: HomeTrucksRVAdapterInterface
    ) {
        binding.addTruckCard.clickToAction(HomeTrucksPriorityAction, item, _interface)
        binding.truckBanner.clickToAction(HomeTrucksPriorityAction, item, _interface)

    }
}

