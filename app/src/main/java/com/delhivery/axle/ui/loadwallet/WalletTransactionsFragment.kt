package com.delhivery.axle.ui.loadwallet

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentWalletTransactionsBinding
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType

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

        binding.rvWalletTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@WalletTransactionsFragment.adapter
        }

        viewModel.historyLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { items ->
                adapter.operation(items)
                binding.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.rvWalletTransactions.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        viewModel.errorLiveData.observe(viewLifecycleOwner, Observer {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvWalletTransactions.visibility = View.GONE
        })

        viewModel.refreshStatusLiveData.observe(viewLifecycleOwner, Observer { result ->
            if (result == null) return@Observer
            val (txnId, newStatus) = result
            viewModel.refreshStatusLiveData.postValue(null)

            val currentItems = adapter.getData()
            val matchingItems = currentItems
                .filterIsInstance<WalletHistoryItem>()
                .filter { it.data.txnNumber == txnId }

            val oldStatus = matchingItems.firstOrNull()?.data?.status
            val statusChanged = oldStatus != null && !oldStatus.equals(newStatus, ignoreCase = true)

            val updatedPairs = matchingItems
                .map { oldItem ->
                    val updatedData = oldItem.data.copy(status = newStatus)
                    Pair(
                        WalletHistoryItem(updatedData) as BaseLoadWalletRVAdapterItem<*>,
                        DataRVAdapterOperationType.Update
                    )
                }
            if (updatedPairs.isNotEmpty()) {
                adapter.operation(updatedPairs)
            }

            val snackMsg = if (statusChanged) "Transaction status updated" else "Status is already up to date"
            (activity as? LoadWalletActivity)?.uiUtils?.showSnackbar(snackMsg)
        })

        viewModel.refreshStatusErrorLiveData.observe(viewLifecycleOwner, Observer {
            val pendingPairs = adapter.getData()
                .filterIsInstance<WalletHistoryItem>()
                .filter { it.data.isPending() }
                .map { Pair(it as BaseLoadWalletRVAdapterItem<*>, DataRVAdapterOperationType.Update) }
            if (pendingPairs.isNotEmpty()) {
                adapter.operation(pendingPairs)
            }
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
                viewModel.refreshTransactionStatus(data.txnNumber, data.dateTime)
            }
            LoadWalletAction_ViewDetails -> {
                val data = (item as? WalletHistoryItem)?.data ?: return
                startActivity(transactionDetailsIntent(requireContext(), data))
            }
        }
    }
}
