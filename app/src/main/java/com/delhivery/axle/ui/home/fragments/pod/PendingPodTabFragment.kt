package com.delhivery.axle.ui.home.fragments.pod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentPendingPodTabBinding
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Pod
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import dagger.android.support.DaggerFragment

/**
 * Fragment for Pending POD tab showing EPOD pending and Physical POD pending items
 */
class PendingPodTabFragment : DaggerFragment() {
    
    private lateinit var binding: FragmentPendingPodTabBinding

    private val adapter: HomePodRVAdapter by lazy { 
        HomePodRVAdapter(object : HomePodRVAdapterInterface {
            override fun handleAction(actionId: String, position: Int, item: BaseHomePodRVAdapterItem<*>) {
                // Delegate to parent fragment if it's HomeNewPodFragment
                (parentFragment as? HomeNewPodFragment)?.let { parent ->
                    // For now, delegate to parent's handleAction if it exists
                    // In a full implementation, you'd want to share the action handling logic
                } ?: run {
                    // If parent is HomePodsFragment, delegate there
                    (parentFragment as? HomePodsFragment)?.handleAction(actionId, position, item)
                }
            }
        })
    }

    private var selectedPodType: PodType = PodType.EPOD // Default to EPOD
    private var epodCount: Int = 0
    private var hpodCount: Int = 0

    enum class PodType {
        EPOD,
        HPOD
    }

    companion object {
        fun newInstance() = PendingPodTabFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pending_pod_tab, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPendingPod.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PendingPodTabFragment.adapter
        }

        // Setup ePOD and HPOD tag selection
        setupPodTypeTags()

        // Observe data from parent fragment's view model
        (parentFragment as? HomeNewPodFragment)?.viewModel?.let { viewModel ->
            viewModel.userPodsData.observe(viewLifecycleOwner, Observer { items ->
                items?.let { _items ->
                    filterAndUpdateItems(_items)
                }
            })

            viewModel.tripsCountLiveData.observe(viewLifecycleOwner, Observer { count ->
                updatePodTypeCounts()
            })
        }

        // Initial data load
        loadPendingPodData()
    }

    private fun setupPodTypeTags() {
        binding.tagEpod.setOnClickListener {
            selectPodType(PodType.EPOD)
        }

        binding.tagHpod.setOnClickListener {
            selectPodType(PodType.HPOD)
        }

        // Set initial selection
        selectPodType(PodType.EPOD)
    }

    private fun selectPodType(type: PodType) {
        selectedPodType = type

        // Update tag UI
        when (type) {
            PodType.EPOD -> {
                binding.tagEpod.setBackgroundResource(R.drawable.bg_pod_tag_selected)
                binding.tagEpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
                binding.tagHpod.setBackgroundResource(R.drawable.bg_pod_tag_unselected)
                binding.tagHpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.font_labels))
            }
            PodType.HPOD -> {
                binding.tagHpod.setBackgroundResource(R.drawable.bg_pod_tag_selected)
                binding.tagHpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
                binding.tagEpod.setBackgroundResource(R.drawable.bg_pod_tag_unselected)
                binding.tagEpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.font_labels))
            }
        }

        // Reload data with selected filter
        loadPendingPodData()
    }

    private fun filterAndUpdateItems(items: List<Pair<com.delhivery.axle.ui.home.fragments.pod.BaseHomePodRVAdapterItem<*>, com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType>>) {
        val filteredItems = items.filter { pair ->
            val item = pair.first
            if (item.type == Pod) {
                val trip = (item as com.delhivery.axle.ui.home.fragments.pod.HomePodTripItem).data
                when (selectedPodType) {
                    PodType.EPOD -> {
                        // Show EPOD pending items (TruckUnloaded status without POD tracking)
                        trip.tripStatus == TruckUnloaded.statusKey && !trip.hasPODTracking()
                    }
                    PodType.HPOD -> {
                        // Show HPOD pending items (EPodUploaded status without POD tracking)
                        trip.tripStatus == EPodUploaded.statusKey && !trip.hasPODTracking()
                    }
                }
            } else {
                true // Keep non-pod items (header, search, etc.)
            }
        }
        adapter.operation(filteredItems)
        updatePodTypeCounts()
    }

    private fun loadPendingPodData() {
        (parentFragment as? HomeNewPodFragment)?.viewModel?.let { viewModel ->
            viewModel.status = when (selectedPodType) {
                PodType.EPOD -> TruckUnloaded
                PodType.HPOD -> EPodUploaded
            }
            viewModel.dispatch = false
            viewModel.fetchTrips()
        }
    }

    private fun updatePodTypeCounts() {
        // Count EPOD and HPOD items from adapter
        val items = adapter.itemsList()
        epodCount = items.count { item ->
            if (item.type == Pod) {
                val trip = (item as com.delhivery.axle.ui.home.fragments.pod.HomePodTripItem).data
                trip.tripStatus == TruckUnloaded.statusKey && !trip.hasPODTracking()
            } else false
        }
        hpodCount = items.count { item ->
            if (item.type == Pod) {
                val trip = (item as com.delhivery.axle.ui.home.fragments.pod.HomePodTripItem).data
                trip.tripStatus == EPodUploaded.statusKey && !trip.hasPODTracking()
            } else false
        }

        binding.tagEpod.text = "ePOD ($epodCount)"
        binding.tagHpod.text = "HPOD ($hpodCount)"
    }
}
