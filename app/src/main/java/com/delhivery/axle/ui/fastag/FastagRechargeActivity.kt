package com.delhivery.axle.ui.fastag

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputFilter
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.view.View
import android.widget.Toast
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFastagRechargeBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.PaymentStatus
import com.delhivery.axle.ui.fastag.wallet.AddMoneyDialogFragment
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.ui.dialogs.PaymentStatusDialogFragment

class FastagRechargeActivity : BaseActivity<ActivityFastagRechargeBinding, FastagRechargeViewModel>() {

    override fun getViewModelClass() = FastagRechargeViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_recharge
    override fun requireConnection() = true

    private var fastagStatus: String? = null
    private var fastagBalanceValue: String? = null
    private var selectedAmount: Double = 0.0
    private var slideStartX = 0f
    private var walletBalance: Double = 0.0
    private val insufficientBalanceHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var insufficientBalanceRunnable: Runnable? = null
    private var currentBottomSheet: PaymentStatusDialogFragment? = null

    companion object {
        const val TAG_ID = "tag_id"
        const val VEHICLE_NUMBER = "vehicle_number"
        const val FASTAG_BALANCE = "fastag_balance"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }

        setupUI()
        setupAmountChips()
        setupAmountInput()
        setupSlideToPayButton()
        setupObservers()

        viewModel.fetchWalletDetails()

        val tagId = intent.getStringExtra(TAG_ID)
        if (!tagId.isNullOrEmpty()) {
            viewModel.fetchFastagStatus(tagId)
        }

        // Listen for the dialog dismissal result
        supportFragmentManager.setFragmentResultListener("PaymentStatusResult", this) { _, bundle ->
            val status = bundle.getString("STATUS")
            if (status == PaymentStatus.SUCCESS.name) {
                // Or the string "SUCCESS" depending on what your Enum passes
                viewModel.fetchWalletDetails()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        insufficientBalanceRunnable?.let { insufficientBalanceHandler.removeCallbacks(it) }
    }

    private fun setupUI() {
        val vehicleNumber = intent.getStringExtra(VEHICLE_NUMBER) ?: ""
        val fastagBalance = intent.getStringExtra(FASTAG_BALANCE) ?: "0"

        binding.tvVehicleNumber.text = vehicleNumber
        binding.tvFastagBalance.text = "FASTag Balance: ₹$fastagBalance"
        fastagBalanceValue = fastagBalance

        binding.ivBack.setOnClickListener { finish() }

        binding.tvAddMoney.setOnClickListener {
            AddMoneyDialogFragment.newInstance(
                redirectUrl = "https://www.delhivery.com/",
                onPaymentResult = {}
            ).show(supportFragmentManager, "AddMoney")
        }

        // Restrict to 6 digits before decimal and 2 digits after decimal
        binding.etAmount.filters = arrayOf(DecimalDigitsInputFilter(6, 2))

        updateAmountDisplay(selectedAmount)
        updateSliderState()
    }

    private fun setupAmountChips() {
        val chips = listOf(
            binding.chip500 to 500.0,
            binding.chip1000 to 1000.0,
            binding.chip1500 to 1500.0
        )

        chips.forEach { (chip, amount) ->
            chip.setOnClickListener {
                selectedAmount = amount
                binding.etAmount.setText(amount.toInt().toString())
                binding.etAmount.setSelection(binding.etAmount.text?.length ?: 0)
                updateChipSelection(amount)
                updateAmountDisplay(amount)
                updateSliderState()
                checkInsufficientBalanceImmediate()
            }
        }
    }

    private fun setupAmountInput() {
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val raw = s?.toString() ?: ""
                val amount = raw.toDoubleOrNull() ?: 0.0
                selectedAmount = amount

                val isZeroEntered = raw.isNotEmpty() && amount == 0.0
                val isLessThanOne = raw.isNotEmpty() && amount > 0.0 && amount < 1.0
                val isInvalid = isZeroEntered || isLessThanOne || amount > 100000
                binding.tvAmountError.visibility = if (isInvalid) View.VISIBLE else View.GONE

                updateChipSelection(amount)
                updateAmountDisplay(amount)
                updateSliderState()
                checkInsufficientBalanceDebounced()
            }
        })
    }

    private fun updateChipSelection(amount: Double) {
        val chipMap = mapOf(
            binding.chip500 to 500.0,
            binding.chip1000 to 1000.0,
            binding.chip1500 to 1500.0
        )

        chipMap.forEach { (chip, chipAmount) ->
            if (chipAmount == amount) {
                chip.setBackgroundResource(R.drawable.bg_amount_chip_selected)
                chip.setTextColor(resources.getColor(android.R.color.white, theme))
            } else {
                chip.setBackgroundResource(R.drawable.bg_amount_chip)
                chip.setTextColor(android.graphics.Color.parseColor("#334155"))
            }
        }
    }

    private fun updateAmountDisplay(amount: Double) {
        val formatted = if (amount == amount.toLong().toDouble()) {
            "₹${amount.toLong()}"
        } else {
            "₹${String.format("%.2f", amount)}"
        }
        binding.tvRechargeAmount.text = formatted
        binding.tvPayableAmount.text = formatted
    }

    private fun updateSliderState() {
        val raw = binding.etAmount.text.toString()
        val hasInsufficientBalance = selectedAmount > 0 && selectedAmount > walletBalance
        val isInvalidAmount = selectedAmount < 1.0 || selectedAmount > 100000
        val enabled = selectedAmount >= 1.0 && !hasInsufficientBalance && !isInvalidAmount
        val container = binding.slideToPayContainer
        val thumb = binding.ivSlideThumb
        val label = binding.tvSlideLabel

        if (enabled) {
            container.setBackgroundResource(R.drawable.bg_slide_to_pay)
            thumb.setColorFilter(resources.getColor(android.R.color.black, theme))
            label.setTextColor(resources.getColor(android.R.color.white, theme))
        } else {
            container.setBackgroundResource(R.drawable.bg_slide_to_pay_disabled)
            val disabledColor = android.graphics.Color.parseColor("#99FFFFFF")
            thumb.setColorFilter(disabledColor)
            label.setTextColor(disabledColor)
        }
    }

    private fun checkInsufficientBalanceDebounced() {
        insufficientBalanceRunnable?.let { insufficientBalanceHandler.removeCallbacks(it) }
        insufficientBalanceRunnable = Runnable { updateInsufficientBalanceUI() }
        insufficientBalanceHandler.postDelayed(insufficientBalanceRunnable!!, 500)
    }

    private fun checkInsufficientBalanceImmediate() {
        insufficientBalanceRunnable?.let { insufficientBalanceHandler.removeCallbacks(it) }
        updateInsufficientBalanceUI()
    }

    private fun updateInsufficientBalanceUI() {
        val shouldShow = selectedAmount > 0 && selectedAmount > walletBalance
        val warning = binding.layoutInsufficientBalance
        val lowBalanceText = binding.tvLowBalance
        val normalBalance = binding.tvWalletBalance

        if (shouldShow) {
            val deficit = selectedAmount - walletBalance
            val deficitFormatted = if (deficit == deficit.toInt().toDouble()) {
                deficit.toInt().toString()
            } else {
                String.format("%.2f", deficit)
            }
            binding.tvInsufficientMessage.text =
                "Please add at least ₹$deficitFormatted to your Delhivery Wallet to recharge your FASTag"

            val balanceFormatted = if (walletBalance == walletBalance.toInt().toDouble()) {
                StringUtils.formatAmount(walletBalance)
            } else {
                StringUtils.formatDecimalAmount(walletBalance)
            }
            lowBalanceText.text = "LOW BALANCE: ₹$balanceFormatted"
            if (lowBalanceText.visibility != View.VISIBLE) {
                lowBalanceText.visibility = View.VISIBLE
                normalBalance.visibility = View.GONE
            }

            if (warning.visibility != View.VISIBLE) {
                warning.translationY = if (warning.height != 0) -warning.height.toFloat() else -100f
                warning.alpha = 0f
                warning.visibility = View.VISIBLE
                warning.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        } else {
            if (lowBalanceText.visibility == View.VISIBLE) {
                lowBalanceText.visibility = View.GONE
                normalBalance.visibility = View.VISIBLE
            }

            if (warning.visibility == View.VISIBLE) {
                warning.animate()
                    .translationY(-warning.height.toFloat())
                    .alpha(0f)
                    .setDuration(250)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction {
                        warning.visibility = View.GONE
                        warning.translationY = 0f
                    }.start()
            }
        }
        updateSliderState()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlideToPayButton() {
        val thumb = binding.ivSlideThumb
        val container = binding.slideToPayContainer

        thumb.setOnTouchListener { view, event ->
            if (selectedAmount < 1.0 || selectedAmount > walletBalance || selectedAmount > 100000) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    slideStartX = event.rawX - view.translationX
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val endMargin = resources.getDimension(R.dimen.size_8dp)
                    val maxSlide = container.width - view.width - endMargin
                    val newX = (event.rawX - slideStartX).coerceIn(0f, maxSlide)
                    view.translationX = newX

                    // Fade the label as user slides
                    val progress = newX / maxSlide
                    binding.tvSlideLabel.alpha = 1f - progress
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val endMargin = resources.getDimension(R.dimen.size_8dp)
                    val maxSlide = container.width - view.width - endMargin
                    val progress = view.translationX / maxSlide

                    if (progress > 0.8f) {
                        // Slide completed - trigger payment
                        view.animate().translationX(maxSlide).setDuration(100).start()
                        onSlideCompleted()
                    } else {
                        // Reset slide
                        view.animate().translationX(0f).setDuration(200).start()
                        binding.tvSlideLabel.animate().alpha(1f).setDuration(200).start()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun onSlideCompleted() {
        if (selectedAmount < 1.0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            resetSlider()
            return
        }

        val tagId = intent.getStringExtra(TAG_ID)
        if (tagId.isNullOrEmpty()) {
            Toast.makeText(this, "FASTag ID not found", Toast.LENGTH_SHORT).show()
            resetSlider()
            return
        }

        showSliderCompletedState()
        viewModel.rechargeFastag(tagId, selectedAmount, fastagStatus, fastagBalanceValue)
    }

    private fun showSliderCompletedState() {
        binding.slideToPayContainer.setBackgroundResource(R.drawable.bg_slide_to_pay_completed)
        binding.ivSlideThumb.visibility = View.GONE
        binding.ivCheckIcon.visibility = View.VISIBLE
        binding.tvSlideLabel.alpha = 1f
        binding.tvSlideLabel.setTextColor(resources.getColor(android.R.color.white, theme))
    }

    private fun resetSlider() {
        binding.ivSlideThumb.translationX = 0f
        binding.ivSlideThumb.visibility = View.VISIBLE
        binding.ivCheckIcon.visibility = View.GONE
        binding.tvSlideLabel.alpha = 1f
        binding.ivSlideThumb.setImageResource(R.drawable.ic_chevron_right_double)
        updateSliderState()
    }


    private fun showPaymentStatusBottomSheet(status: PaymentStatus) {
        // Dismiss existing bottom sheet if any
        currentBottomSheet?.dismissAllowingStateLoss()
        currentBottomSheet = PaymentStatusDialogFragment.show(
            fragmentManager = supportFragmentManager,
            initialStatus = status,
            onDismiss = {
                resetSlider()
                currentBottomSheet = null
            }
        )
    }

    private fun setupObservers() {
        viewModel.walletBalanceData.observe(this, androidx.lifecycle.Observer { balance ->
            walletBalance = balance
            val balanceFormatted = if (balance == balance.toInt().toDouble()) {
                StringUtils.formatAmount(balance)
            } else {
                StringUtils.formatDecimalAmount(balance)
            }
            binding.tvWalletBalance.text = "₹$balanceFormatted"
            checkInsufficientBalanceImmediate()
        })

        viewModel.rechargeResponseData.observe(this, androidx.lifecycle.Observer { response ->
            response?.let {
                resetSlider()
                // Update wallet balance on screen
                it.updatedWalletBalance?.let { newBalance ->
                    walletBalance = newBalance
                    val balanceFormatted = if (newBalance == newBalance.toInt().toDouble()) {
                        StringUtils.formatAmount(newBalance)
                    } else {
                        StringUtils.formatDecimalAmount(newBalance)
                    }
                    binding.tvWalletBalance.text = "₹$balanceFormatted"
                    checkInsufficientBalanceImmediate()
                }
                // Update FASTag balance on screen
                it.fastagBalance?.let { newFastagBalance ->
                    binding.tvFastagBalance.text = "FASTag Balance: ₹$newFastagBalance"
                    fastagBalanceValue = newFastagBalance
                }

                val status = when (it.status?.lowercase()) {
                    "success" -> PaymentStatus.SUCCESS
                    "pending", "processing" -> PaymentStatus.PENDING
                    "failed", "failure" -> PaymentStatus.FAILURE
                    else -> PaymentStatus.SUCCESS
                }
                showPaymentStatusBottomSheet(status)
            }
        })

        viewModel.fastagBlacklistedData.observe(this, androidx.lifecycle.Observer { isBlacklisted ->
            fastagStatus = if (isBlacklisted) "blacklisted" else "active"
            binding.layoutBlacklistWarning.visibility = if (isBlacklisted) View.VISIBLE else View.GONE
        })

        viewModel.exceptionLiveData.observe(this, androidx.lifecycle.Observer {
            it?.let { resetSlider() }
        })
    }

    /**
     * InputFilter that restricts input to [maxDigitsBefore] digits before the decimal
     * and [maxDigitsAfter] digits after the decimal.
     */
    private class DecimalDigitsInputFilter(
        private val maxDigitsBefore: Int,
        private val maxDigitsAfter: Int
    ) : InputFilter {
        override fun filter(
            source: CharSequence, start: Int, end: Int,
            dest: Spanned, dstart: Int, dend: Int
        ): CharSequence? {
            val result = dest.toString().substring(0, dstart) +
                source.toString().substring(start, end) +
                dest.toString().substring(dend)

            if (result.isEmpty()) return null

            val dotIndex = result.indexOf('.')
            if (dotIndex == -1) {
                if (result.length > maxDigitsBefore) return ""
            } else {
                val integerPart = result.substring(0, dotIndex)
                val decimalPart = result.substring(dotIndex + 1)
                if (integerPart.length > maxDigitsBefore) return ""
                if (decimalPart.length > maxDigitsAfter) return ""
            }
            return null
        }
    }
}
