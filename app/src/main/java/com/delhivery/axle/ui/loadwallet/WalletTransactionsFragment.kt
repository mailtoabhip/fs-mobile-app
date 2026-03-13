package com.delhivery.axle.ui.loadwallet

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentWalletTransactionsBinding
import com.delhivery.axle.ui.base.BaseFragment

/**
 * Fragment showing wallet transaction history
 */
class WalletTransactionsFragment : BaseFragment<FragmentWalletTransactionsBinding, LoadWalletViewModel>(),
    LoadWalletRVAdapterInterface {

    companion object {
        val _instance by lazy { WalletTransactionsFragment() }
    }

    private val adapter: LoadWalletRVAdapter by lazy {
        LoadWalletRVAdapter(this)
    }

    override fun getViewModelClass() = LoadWalletViewModel::class.java

    override fun layoutId() = R.layout.fragment_wallet_transactions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        binding.rvWalletTransactions.apply {
            this.layoutManager = layoutManager
            adapter = this@WalletTransactionsFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    val total = layoutManager.itemCount
                    if (lastVisible >= total - 3) {
                        viewModel.loadNextTransactionPage()
                    }
                }
            })
        }

        viewModel.historyLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { items ->
                val isInitialLoading = viewModel.transactionInitialLoadingLiveData.value == true
                adapter.submitList(items) {
                    if (!isInitialLoading) {
                        binding.emptyState.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                        binding.rvWalletTransactions.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
                    }
                }
            }
        })

        viewModel.transactionLoadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            val isInitialLoading = viewModel.transactionInitialLoadingLiveData.value == true
            binding.progressLoadMore.visibility = if (isLoading == true && !isInitialLoading) View.VISIBLE else View.GONE
        })

        viewModel.transactionInitialLoadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressInitialLoad.visibility = if (isLoading == true) View.VISIBLE else View.GONE
            if (isLoading == true) {
                binding.progressLoadMore.visibility = View.GONE
                binding.rvWalletTransactions.visibility = View.GONE
                binding.emptyState.visibility = View.GONE
            }
        })

        viewModel.errorLiveData.observe(viewLifecycleOwner, Observer {
            val isInitialLoading = viewModel.transactionInitialLoadingLiveData.value == true
            if (!isInitialLoading) {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvWalletTransactions.visibility = View.GONE
            }
        })

        viewModel.refreshStatusLiveData.observe(viewLifecycleOwner, Observer { result ->
            if (result == null) return@Observer
            val (txnId, newStatus) = result
            viewModel.refreshStatusLiveData.postValue(null)

            val oldStatus = adapter.getData()
                .filterIsInstance<WalletHistoryItem>()
                .firstOrNull { it.data.txnNumber == txnId }?.data?.status

            adapter.updateItem(txnId, newStatus)

            val statusChanged = oldStatus != null && !oldStatus.equals(newStatus, ignoreCase = true)
            val snackMsg = if (statusChanged) "Transaction status updated" else "Status is already up to date"
            (activity as? LoadWalletActivity)?.uiUtils?.showSnackbar(snackMsg)
        })

        viewModel.refreshStatusErrorLiveData.observe(viewLifecycleOwner, Observer { txnId ->
            if (txnId == null) return@Observer
            viewModel.refreshStatusErrorLiveData.postValue(null)
            adapter.clearRefreshing(txnId)
            (activity as? LoadWalletActivity)?.dialogUtils?.showErrorDialog(
                "Unable to refresh status", 3L
            )
        })
    }

    fun refreshData() {
        adapter.resetData()
        val activity = activity as? LoadWalletActivity
        viewModel.fetchWalletData(activity?.currentFilter ?: WalletFilter())
    }

    fun applyFilter(filter: WalletFilter) {
        adapter.resetData()
        viewModel.fetchWalletData(filter)
    }

    override fun handleAction(
        actionId: String,
        item: BaseLoadWalletRVAdapterItem<*>
    ) {
        when (actionId) {
            LoadWalletAction_RefreshStatus -> {
                val data = (item as? WalletHistoryItem)?.data ?: return
                adapter.setRefreshing(data.txnNumber)
                viewModel.refreshTransactionStatus(data.txnNumber, data.dateTime)
            }
            LoadWalletAction_ViewDetails -> {
                val data = (item as? WalletHistoryItem)?.data ?: return
                startActivity(transactionDetailsIntent(requireContext(), data))
            }
        }
    }
}
