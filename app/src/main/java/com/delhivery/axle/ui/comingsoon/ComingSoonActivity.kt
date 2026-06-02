package com.delhivery.axle.ui.comingsoon

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityComingSoonBinding
import com.delhivery.axle.ui.base.BaseActivity

class ComingSoonActivity : BaseActivity<ActivityComingSoonBinding, ComingSoonViewModel>() {

    override fun getViewModelClass() = ComingSoonViewModel::class.java
    override fun layoutId() = R.layout.activity_coming_soon
    override fun requireConnection() = false

    private var isNotified = false

    companion object {
        const val EXTRA_FEATURE_TYPE = "feature_type"
        const val TYPE_GPS = "gps"
        const val TYPE_FUEL_CARD = "fuel_card"

        fun newIntent(context: Context, featureType: String): Intent {
            return Intent(context, ComingSoonActivity::class.java).apply {
                putExtra(EXTRA_FEATURE_TYPE, featureType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val featureType = intent.getStringExtra(EXTRA_FEATURE_TYPE) ?: TYPE_GPS
        setupUI(featureType)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    private fun setupUI(featureType: String) {
        val isGps = featureType == TYPE_GPS

        binding.tvTitle.text = if (isGps) getString(R.string.label_gps) else getString(R.string.label_fuel_card_home)

        binding.ivFeatureIcon.setImageResource(
            if (isGps) R.drawable.ic_gps_icon else R.drawable.card
        )

        binding.tvFeatureTitle.text = getString(
            R.string.coming_soon_title,
            if (isGps) getString(R.string.label_gps) else getString(R.string.label_fuel_card_home)
        )

        binding.tvFeatureSubtitle.text = if (isGps) {
            getString(R.string.coming_soon_gps_subtitle)
        } else {
            getString(R.string.coming_soon_fuel_card_subtitle)
        }

        binding.tvSuccessMessage.text = if (isGps) {
            getString(R.string.coming_soon_gps_notified)
        } else {
            getString(R.string.coming_soon_fuel_card_notified)
        }

        showDefaultState()

        binding.ivBack.setOnClickListener { finish() }

        binding.btnPrimary.setOnClickListener {
            finish()
//            if (isNotified) {
//                finish()
//            } else {
//                onNotifyMeClicked()
//            }
        }

//        binding.btnSecondary.setOnClickListener {
//            finish()
//        }
    }

    private fun showDefaultState() {
        isNotified = false
        binding.successBanner.visibility = View.GONE
        binding.btnPrimary.text = getString(R.string.coming_soon_go_to_homepage)
//        binding.btnSecondary.visibility = View.VISIBLE
//        binding.btnSecondary.text = getString(R.string.coming_soon_remind_later)
    }

    private fun onNotifyMeClicked() {
        isNotified = true
        binding.successBanner.visibility = View.VISIBLE
        binding.btnPrimary.text = getString(R.string.coming_soon_go_to_homepage)
//        binding.btnSecondary.visibility = View.GONE
    }
}
