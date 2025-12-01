package com.delhivery.axle.ui.home.fragments.pod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentSubmittedPodTabBinding
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import dagger.android.support.DaggerFragment

/**
 * Fragment for Submitted tab showing Dispatched items
 */
class SubmittedPodTabFragment : DaggerFragment() {
    
    private lateinit var binding: FragmentSubmittedPodTabBinding

    private val adapter: HomePodRVAdapter by lazy { 
        HomePodRVAdapter(object : HomePodRVAdapterInterface {
            override fun handleAction(actionId: String, position: Int, item: BaseHomePodRVAdapterItem<*>) {
                // Delegate to parent fragment if it's HomeNewPodFragment
                (parentFragment as? HomeNewPodFragment)?.let { parent ->
                    // For now, delegate to parent's handleAction if it exists
                } ?: run {
                    // If parent is HomePodsFragment, delegate there
                    (parentFragment as? HomePodsFragment)?.handleAction(actionId, position, item)
                }
            }
        })
    }

    companion object {
        fun newInstance() = SubmittedPodTabFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_submitted_pod_tab, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSubmittedPod.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SubmittedPodTabFragment.adapter
        }

        // Observe data from parent fragment's view model
        (parentFragment as? HomeNewPodFragment)?.viewModel?.let { viewModel ->
            viewModel.userPodsData.observe(viewLifecycleOwner, Observer { items ->
                items?.let { _items ->
                    filterAndUpdateItems(_items)
                }
            })
        }

        // Initial data load
        loadSubmittedPodData()
    }

    private fun filterAndUpdateItems(items: List<Pair<BaseHomePodRVAdapterItem<*>, com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType>>) {
        val filteredItems = items.filter { pair ->
            val item = pair.first
            if (item.type == HomePodRVAdapterItemType.Pod) {
                val trip = (item as HomePodTripItem).data
                // Show dispatched items (EPodUploaded status with POD tracking)
                trip.tripStatus == EPodUploaded.statusKey && trip.hasPODTracking()
            } else {
                true // Keep non-pod items
            }
        }
        adapter.operation(filteredItems)
    }

    private fun loadSubmittedPodData() {
        (parentFragment as? HomeNewPodFragment)?.viewModel?.let { viewModel ->
            viewModel.status = EPodUploaded
            viewModel.dispatch = true // Show dispatched items
            viewModel.fetchTrips()
        }
    }
}
