package com.delhivery.axle.ui.loadwallet

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentWalletRechargesBinding
import com.delhivery.axle.ui.base.BaseFragment

/**
 * Fragment showing wallet recharge history
 */
class WalletRechargesFragment : BaseFragment<FragmentWalletRechargesBinding, LoadWalletViewModel>(),
    LoadWalletRVAdapterInterface {

    companion object {
        val _instance by lazy { WalletRechargesFragment() }
    }

    private val adapter: LoadWalletRVAdapter by lazy {
        LoadWalletRVAdapter(this)
    }

    override fun getViewModelClass() = LoadWalletViewModel::class.java

    override fun layoutId() = R.layout.fragment_wallet_recharges

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        binding.rvWalletRecharges.apply {
            this.layoutManager = layoutManager
            adapter = this@WalletRechargesFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    val total = layoutManager.itemCount
                    if (lastVisible >= total - 3) {
                        viewModel.loadNextRechargePage()
                    }
                }
            })
        }

        viewModel.rechargesLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { items ->
                val isInitialLoading = viewModel.rechargeInitialLoadingLiveData.value == true
                adapter.submitList(items) {
                    if (!isInitialLoading) {
                        binding.emptyState.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                        binding.rvWalletRecharges.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
                    }
                }
            }
        })

        viewModel.rechargeLoadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            val isInitialLoading = viewModel.rechargeInitialLoadingLiveData.value == true
            binding.progressLoadMore.visibility = if (isLoading == true && !isInitialLoading) View.VISIBLE else View.GONE
        })

        viewModel.rechargeInitialLoadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressInitialLoad.visibility = if (isLoading == true) View.VISIBLE else View.GONE
            if (isLoading == true) {
                binding.progressLoadMore.visibility = View.GONE
                binding.rvWalletRecharges.visibility = View.GONE
                binding.emptyState.visibility = View.GONE
            }
        })

        viewModel.errorLiveData.observe(viewLifecycleOwner, Observer {
            val isInitialLoading = viewModel.rechargeInitialLoadingLiveData.value == true
            if (!isInitialLoading) {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvWalletRecharges.visibility = View.GONE
            }
        })

        viewModel.refreshRechargeStatusLiveData.observe(viewLifecycleOwner, Observer { result ->
            if (result == null) return@Observer
            val (rechargeId, newStatus) = result
            viewModel.refreshRechargeStatusLiveData.postValue(null)

            val oldStatus = adapter.getData()
                .filterIsInstance<WalletHistoryItem>()
                .firstOrNull { it.data.txnNumber == rechargeId }?.data?.status

            adapter.updateItem(rechargeId, newStatus)

            val statusChanged = oldStatus != null && !oldStatus.equals(newStatus, ignoreCase = true)
            val snackMsg = if (statusChanged) "Transaction status updated" else "Status is already up to date"
            (activity as? LoadWalletActivity)?.uiUtils?.showSnackbar(snackMsg)
        })

        viewModel.refreshRechargeStatusErrorLiveData.observe(viewLifecycleOwner, Observer { txnId ->
            if (txnId == null) return@Observer
            viewModel.refreshRechargeStatusErrorLiveData.postValue(null)
            adapter.clearRefreshing(txnId)
            (activity as? LoadWalletActivity)?.dialogUtils?.showErrorDialog(
                "Unable to refresh status", 3L
            )
        })
    }

    fun refreshData() {
        adapter.resetData()
        val activity = activity as? LoadWalletActivity
        viewModel.fetchRecharges(activity?.currentFilter ?: WalletFilter())
    }

    override fun handleAction(
        actionId: String,
        item: BaseLoadWalletRVAdapterItem<*>
    ) {
        when (actionId) {
            LoadWalletAction_RefreshStatus -> {
                val data = (item as? WalletHistoryItem)?.data ?: return
                adapter.setRefreshing(data.txnNumber)
                viewModel.refreshRechargeStatus(data.txnNumber, data.dateTime)
            }
            LoadWalletAction_ViewDetails -> {
                val data = (item as? WalletHistoryItem)?.data ?: return
                startActivity(rechargeDetailsIntent(requireContext(), data))
            }
        }
    }
}
