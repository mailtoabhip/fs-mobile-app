package com.delhivery.axle.ui.invoicereview

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityInvoiceReviewBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.WindowInsetsUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity for GST vendors to review and accept/reject invoices
 */
class InvoiceReviewActivity : BaseActivity<ActivityInvoiceReviewBinding, InvoiceReviewViewModel>(), DatePickerDialog.OnDateSetListener {

    companion object {
        const val EXTRA_TRANSACTION_ID = "transaction_id"
        const val RESULT_INVOICE_ACCEPTED = 100
        const val RESULT_INVOICE_REJECTED = 101

        fun invoiceReviewIntent(transactionId: String, context: Context): Intent {
            return Intent(context, InvoiceReviewActivity::class.java).apply {
                putExtra(EXTRA_TRANSACTION_ID, transactionId)
            }
        }
    }

    override fun getViewModelClass() = InvoiceReviewViewModel::class.java
    override fun layoutId() = R.layout.activity_invoice_review

    private lateinit var particularsAdapter: InvoiceParticularAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use dummy ID for testing if not provided
        val transactionId = intent?.getStringExtra(EXTRA_TRANSACTION_ID) ?: "TEST_TRANSACTION_123"

        viewModel.transactionId = transactionId

        setupToolbar()
        setupRecyclerView()
        setupDatePicker()
        setupClickListeners()
        setupBackNavigation()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Handle window insets for edge-to-edge display (API 35+)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }

        observeViewModel()
        viewModel.fetchInvoiceDetails()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        particularsAdapter = InvoiceParticularAdapter()
        binding.rvInvoiceParticulars.apply {
            layoutManager = LinearLayoutManager(this@InvoiceReviewActivity)
            adapter = particularsAdapter
        }
    }

    private fun setupDatePicker() {
        // Prefill with current date
        val today = Calendar.getInstance()
        val currentDate = "${today.get(Calendar.DAY_OF_MONTH)}/${today.get(Calendar.MONTH) + 1}/${today.get(Calendar.YEAR)}"
        binding.tvInvoiceDate.setText(currentDate)
        
        binding.tvInvoiceDate.setOnClickListener {
            dialogUtils.datePicker(this, minDate = -30, maxDate = 30)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        binding.tvInvoiceDate.setText("$dayOfMonth/${monthOfYear}/$year")
    }

    private fun setupClickListeners() {
        binding.btnAccept.setOnClickListener {
            val invoiceNumber = binding.etInvoiceNo.text?.toString() ?: ""
            val invoiceDate = binding.tvInvoiceDate.text?.toString() ?: ""

            if (viewModel.validateInputs(invoiceNumber, invoiceDate)) {
                showAcceptConfirmationDialog(invoiceNumber, invoiceDate)
            }
        }

        binding.btnReject.setOnClickListener {
            showRejectConfirmationDialog()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun observeViewModel() {
        viewModel.invoiceDetailsLiveData.observe(this, Observer { invoice ->
            binding.invoice = invoice
            particularsAdapter.submitList(invoice.invoiceParticulars)
        })

        viewModel.isLoadingLiveData.observe(this, Observer { isLoading ->
            if (isLoading) {
                uiUtils.showProgress()
                binding.btnAccept.isEnabled = false
                binding.btnReject.isEnabled = false
            } else {
                uiUtils.hideProgress()
                binding.btnAccept.isEnabled = true
                binding.btnReject.isEnabled = true
            }
        })

        viewModel.invoiceAcceptedLiveData.observe(this, Observer { success ->
            if (success) {
                uiUtils.showSnackbar("Invoice accepted successfully")
                setResult(RESULT_INVOICE_ACCEPTED)
                finish()
            }
        })

        viewModel.invoiceRejectedLiveData.observe(this, Observer { success ->
            if (success) {
                uiUtils.showSnackbar("Invoice rejected. Please contact the centre for re-raising invoice acceptance request.")
                setResult(RESULT_INVOICE_REJECTED)
                finish()
            }
        })

        viewModel.errorLiveData.observe(this, Observer { error ->
            uiUtils.showSnackbar(error)
        })

        viewModel.alreadyProcessedLiveData.observe(this, Observer { alreadyProcessed ->
            if (alreadyProcessed) {
                uiUtils.showSnackbar("This invoice has already been processed")
                viewModel.fetchInvoiceDetails() // Refresh to get latest status
            }
        })
    }

    private fun showAcceptConfirmationDialog(invoiceNumber: String, invoiceDate: String) {
        InvoiceConfirmationDialog(
            context = this,
            type = InvoiceConfirmationDialog.ConfirmationType.ACCEPT,
            onConfirm = {
                viewModel.acceptRejectInvoice(confirmationType = InvoiceConfirmationDialog.ConfirmationType.ACCEPT, invoiceNumber, invoiceDate)
            }
        ).show()
    }

    private fun showRejectConfirmationDialog() {
        val invoiceNumber = binding.etInvoiceNo.text?.toString()
        val invoiceDate = binding.tvInvoiceDate.text?.toString()
        
        InvoiceConfirmationDialog(
            context = this,
            type = InvoiceConfirmationDialog.ConfirmationType.REJECT,
            onConfirm = {
                viewModel.acceptRejectInvoice(
                    confirmationType = InvoiceConfirmationDialog.ConfirmationType.REJECT,
                    invoiceNumber = invoiceNumber,
                    invoiceDate = invoiceDate
                )
            }
        ).show()
    }
}
