package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityPaymentBreakupBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.PaymentStatus
import com.delhivery.axle.ui.fastag.wallet.AddMoneyDialogFragment

class PaymentBreakupActivity : BaseActivity<ActivityPaymentBreakupBinding, PaymentBreakupViewModel>() {

    override fun getViewModelClass() = PaymentBreakupViewModel::class.java
    override fun layoutId() = R.layout.activity_payment_breakup
    override fun requireConnection() = true

    private var salesCode = ""
    private var paymentMethod = ""
    private var items: List<com.delhivery.axle.api.request.PaymentBreakupItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupClickListeners()

        // Fetch payment breakup from API
        salesCode = intent.getStringExtra(EXTRA_SALES_CODE) ?: ""
        paymentMethod = intent.getStringExtra(EXTRA_PAYMENT_METHOD) ?: "FULL_PAYMENT"
        @Suppress("DEPRECATION")
        items = intent.getSerializableExtra(EXTRA_ITEMS) as? ArrayList<com.delhivery.axle.api.request.PaymentBreakupItem> ?: arrayListOf()

        observeViewModel()
        viewModel.fetchPaymentBreakup(salesCode, paymentMethod, items)

        // Listen for wallet recharge success from PaymentStatusDialogFragment
        supportFragmentManager.setFragmentResultListener("PaymentStatusResult", this) { _, bundle ->
            val status = bundle.getString("STATUS")
            if (status == PaymentStatus.SUCCESS.name) {
                // Re-fetch breakup to update wallet balance and toggle slide-to-confirm
                viewModel.fetchPaymentBreakup(salesCode, paymentMethod, items)
            }
        }
    }

    companion object {
        const val EXTRA_SALES_CODE = "extra_sales_code"
        const val EXTRA_PAYMENT_METHOD = "extra_payment_method"
        const val EXTRA_ITEMS = "extra_items"
    }

    private fun observeViewModel() {
        viewModel.breakupState.observe(this) { resource ->
            when (resource) {
                is com.delhivery.axle.api.repository.Resource.Loading -> {
                    // TODO: Show loading
                }
                is com.delhivery.axle.api.repository.Resource.Success -> {
                    resource.data?.let { data ->
                        binding.rvBreakup.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
                        binding.rvBreakup.adapter = BreakupLineAdapter(data.breakup)
                    }
                }
                is com.delhivery.axle.api.repository.Resource.Failure -> {
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Payment Breakup"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupClickListeners() {
        binding.btnProceedToPay.setOnClickListener {
            // Disabled state — wallet insufficient
        }

        binding.tvAddMoney.setOnClickListener {
            showAddMoneyBottomSheet()
        }

        binding.slideToConfirm.setOnSlideCompleteListener {
            // Payment deducted from wallet — show success bottom sheet
            showPaymentSuccessBottomSheet()
        }
    }

    private fun showPaymentSuccessBottomSheet() {
        PaymentSuccessBottomSheet.newInstance {
            val intent = Intent(this, FastagCollectionActivity::class.java).apply {
                putExtra("extra_sales_code", salesCode)
            }
            startActivity(intent)
            finish()
        }.show(supportFragmentManager, PaymentSuccessBottomSheet.TAG)
    }

    private fun showAddMoneyBottomSheet() {
        AddMoneyDialogFragment.newInstance(
            redirectUrl = "https://www.delhivery.com/",
            onPaymentResult = { success ->
                // Note: Actual refresh is handled by PaymentStatusResult fragment result listener
            }
        ).show(supportFragmentManager, "AddMoneyDialog")
    }
}
