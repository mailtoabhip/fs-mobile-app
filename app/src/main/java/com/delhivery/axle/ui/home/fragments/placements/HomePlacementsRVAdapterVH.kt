package com.delhivery.axle.ui.home.fragments.placements

import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.data.home.placements.HomePlacementsFilterDelay
import com.delhivery.axle.data.home.placements.HomePlacementsFilterExpected
import com.delhivery.axle.data.home.placements.HomePlacementsFilterMissing
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewIntercityAdhocBinding
import com.delhivery.axle.databinding.ViewIntercityContractBinding
import com.delhivery.axle.databinding.ViewIntracityContractBinding
import com.delhivery.axle.databinding.ViewIntractiyAdhocBinding
import com.delhivery.axle.databinding.ViewPlacementsDurationsBinding
import com.delhivery.axle.databinding.ViewPlacementsFiltersBinding
import com.delhivery.axle.databinding.ViewPlacementsNoDelayBinding
import com.delhivery.axle.databinding.ViewPlacementsTypeBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

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

internal class HomePlacementsIntracityAdhocRequestItemVH(binding:ViewIntractiyAdhocBinding) :
    BaseHomePlacementsRVAdapterViewHolder<ViewIntractiyAdhocBinding, HomePlacementsIntracityAdhocRequestItem>(binding) {
    override fun bind(
        item: HomePlacementsIntracityAdhocRequestItem,
        _interface: HomePlacementsRVAdapterInterface
    ) {
        binding.request = item.data
        binding.driverTruckDetails.request = item.data
        when(item.data.status){
            "Delayed"-> {
                binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_delayed)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.light_orange_color))

            }
            "Marked-in"-> {  binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_marked)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.bid_placed_green))

            }
            "Expected"-> {  binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_expected)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))

            }

        }



    }
}

internal class HomePlacementsIntracityContractRequestItemVH(binding:ViewIntracityContractBinding) :
    BaseHomePlacementsRVAdapterViewHolder<ViewIntracityContractBinding, HomePlacementsIntracityContractsRequestItem>(binding) {
    override fun bind(
        item: HomePlacementsIntracityContractsRequestItem,
        _interface: HomePlacementsRVAdapterInterface
    ) {
        binding.request = item.data
        binding.driverTruckDetails.request = item.data
        when(item.data.status){
            "Delayed"-> {
                binding.statusText.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_all_round_delayed)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.light_orange_color))

            }
            "Marked-in"-> {  binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_marked)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.bid_placed_green))

            }
            "Expected"-> {  binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_expected)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))

            }

        }
    }
}

internal class HomePlacementsIntercityAdhocRequestItemVH(binding:ViewIntercityAdhocBinding) :
    BaseHomePlacementsRVAdapterViewHolder<ViewIntercityAdhocBinding, HomePlacementsIntercityAdhocRequestItem>(binding) {
    override fun bind(
        item: HomePlacementsIntercityAdhocRequestItem,
        _interface: HomePlacementsRVAdapterInterface
    ) {
        binding.request = item.data
        binding.driverTruckDetails.request = item.data
        binding.originDestination.request = item.data
        when(item.data.status){
            "Delayed"-> {
                binding.statusText.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_all_round_delayed)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.light_orange_color))

            }
            "Marked-in"-> {  binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_marked)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.bid_placed_green))

            }
            "Expected"-> {  binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_expected)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))

            }

        }
    }
}

internal class HomePlacementsIntercityContractRequestItemVH(binding:ViewIntercityContractBinding) :
    BaseHomePlacementsRVAdapterViewHolder<ViewIntercityContractBinding, HomePlacementsIntercityContractsRequestItem>(binding) {
    override fun bind(
        item: HomePlacementsIntercityContractsRequestItem,
        _interface: HomePlacementsRVAdapterInterface
    ) {
        binding.request = item.data
        binding.driverTruckDetails.request = item.data
        binding.originDestination.request = item.data
        when(item.data.status){
            "Delayed"-> {
                binding.statusText.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_all_round_delayed)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.light_orange_color))

            }
            "Marked-in"-> {  binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_marked)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.bid_placed_green))

            }
            "Expected"-> {  binding.statusText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_expected)
                binding.statusText.setTextColor(ContextCompat.getColor(context, R.color.bid_under_review))

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
        if (binding.durationText.text==PlacementTypes.Delayed.name){
            binding.durationText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_orange_boundary)
        }else{
            binding.durationText.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_dark_grey_boundary)
        }
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


