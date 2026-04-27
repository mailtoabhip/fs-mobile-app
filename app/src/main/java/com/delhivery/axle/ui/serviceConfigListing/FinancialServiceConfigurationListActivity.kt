package com.delhivery.axle.ui.serviceConfigListing

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.ui.dialogs.FinancialServiceInfoBottomSheetDialogFragment

class FinancialServiceConfigurationListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FinancialServiceConfigurationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial_service_configuration_list)

        setupRecyclerView()
        setupBackButton()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.configure_new_service_rv)

        val services = listOf(
            ServiceConfigurationModel(
                id = "fastag",
                title = getString(R.string.financial_service_fastag_title),
                description = getString(R.string.financial_service_fastag_description),
                iconResId = R.drawable.ic_fastag_home
            ),
            ServiceConfigurationModel(
                id = "delhivery_wallet",
                title = getString(R.string.financial_service_wallet_title),
                description = getString(R.string.financial_service_wallet_description),
                iconResId = R.drawable.ic_delhivery_wallet
            ),
            ServiceConfigurationModel(
                id = "fuel_credit",
                title = getString(R.string.financial_service_fuel_title),
                description = getString(R.string.financial_service_fuel_description),
                iconResId = R.drawable.ic_delhivery_bolt
            )
        )

        adapter = FinancialServiceConfigurationAdapter(
            services = services,
            onKnowMoreClick = { service ->
                when (service.id) {
                    "fastag" -> showFastagInfoBottomSheet()
                    else -> Toast.makeText(this, "Know More: ${service.title}", Toast.LENGTH_SHORT).show()
                }
            },
            onConfigureClick = { service ->
                Toast.makeText(this, "Configure: ${service.title}", Toast.LENGTH_SHORT).show()
                // Handle configure click
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun showFastagInfoBottomSheet() {
        FinancialServiceInfoBottomSheetDialogFragment.newInstance()
            .show(supportFragmentManager, "FinancialServiceInfoBottomSheet")
    }

    private fun setupBackButton() {
        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }
}
