package com.dfd.delfin.ui.serviceConfigListing

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.R
import com.dfd.delfin.ui.dialogs.FinancialServiceInfoBottomSheetDialogFragment

class ServiceConfigurationListActivity : AppCompatActivity() {

    private lateinit var servicesAddedRecyclerView: RecyclerView
    private lateinit var servicesAddedAdapter: ServiceConfigurationAdapter

    private lateinit var continueWhereLeftRecyclerView: RecyclerView
    private lateinit var continueWhereLeftAdapter: ServiceConfigurationAdapter

    private lateinit var loadsRecyclerView: RecyclerView
    private lateinit var loadsAdapter: ServiceConfigurationAdapter

    private lateinit var financialRecyclerView: RecyclerView
    private lateinit var financialAdapter: ServiceConfigurationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_configuration_list)

        setupServicesAddedRecyclerView()
        setupContinueWhereLeftRecyclerView()
        setupLoadsRecyclerView()
        setupFinancialRecyclerView()
        setupBackButton()
    }

    private fun setupServicesAddedRecyclerView() {
        servicesAddedRecyclerView = findViewById(R.id.services_added_rv)

        val services = listOf(
            ServiceConfigurationModel(
                id = "fleet_linehaul_added",
                title = "Fleet Linehaul",
                description = "Full truck load services for Long-haul intercity movement",
                iconResId = R.drawable.ic_delhivery_load,
                status = ServiceStatus(
                    statusText = "Active",
                    statusDescription = "Service Configured",
                    statusType = StatusType.COMPLETED
                )
            ),
            ServiceConfigurationModel(
                id = "marketplace_loads_added",
                title = "Marketplace Loads",
                description = "Access loads from the open marketplace",
                iconResId = R.drawable.ic_delhivery_load,
                status = ServiceStatus(
                    statusText = "Active",
                    statusDescription = "Service Configured",
                    statusType = StatusType.COMPLETED
                )
            )
        )

        servicesAddedAdapter = ServiceConfigurationAdapter(
            services = services,
            onKnowMoreClick = { service ->
                Toast.makeText(this, "Know More: ${service.title}", Toast.LENGTH_SHORT).show()
            },
            onConfigureClick = { service ->
                Toast.makeText(this, "Configure: ${service.title}", Toast.LENGTH_SHORT).show()
            }
        )

        servicesAddedRecyclerView.layoutManager = LinearLayoutManager(this)
        servicesAddedRecyclerView.adapter = servicesAddedAdapter
    }

    private fun setupContinueWhereLeftRecyclerView() {
        continueWhereLeftRecyclerView = findViewById(R.id.continue_service_rv)

        val services = listOf(
            ServiceConfigurationModel(
                id = "fleet_carting_inprogress",
                title = "Fleet Carting",
                description = "Intracity fleet operations with own vehicles",
                iconResId = R.drawable.ic_delhivery_load,
                status = ServiceStatus(
                    statusText = "In Progress",
                    statusDescription = "1/3 Sections Completed",
                    statusType = StatusType.IN_PROGRESS
                )
            )
        )

        continueWhereLeftAdapter = ServiceConfigurationAdapter(
            services = services,
            onKnowMoreClick = { service ->
                Toast.makeText(this, "Know More: ${service.title}", Toast.LENGTH_SHORT).show()
            },
            onConfigureClick = { service ->
                Toast.makeText(this, "Configure: ${service.title}", Toast.LENGTH_SHORT).show()
            }
        )

        continueWhereLeftRecyclerView.layoutManager = LinearLayoutManager(this)
        continueWhereLeftRecyclerView.adapter = continueWhereLeftAdapter
    }

    private fun setupLoadsRecyclerView() {
        loadsRecyclerView = findViewById(R.id.configure_new_service_rv)

        val services = listOf(
            ServiceConfigurationModel(
                id = "fleet_carting",
                title = "Fleet Carting",
                description = "Intracity fleet operations with own vehicles",
                iconResId = R.drawable.ic_delhivery_load,
                status = ServiceStatus(
                    statusText = "In Progress",
                    statusDescription = "1/3 Sections Completed",
                    statusType = StatusType.IN_PROGRESS
                )
            ),
            ServiceConfigurationModel(
                id = "ftl_loads",
                title = "FTL Loads",
                description = "Full truck load services (GST registered businesses)",
                iconResId = R.drawable.ic_delhivery_load
            ),
            ServiceConfigurationModel(
                id = "fleet_linehaul",
                title = "Fleet Linehaul",
                description = "Full truck load services for Long-haul intercity movement",
                iconResId = R.drawable.ic_delhivery_load
            ),
            ServiceConfigurationModel(
                id = "marketplace_loads",
                title = "Marketplace Loads",
                description = "Access loads from the open marketplace",
                iconResId = R.drawable.ic_delhivery_load
            ),
            ServiceConfigurationModel(
                id = "ops_arranged_loads",
                title = "Ops Arranged Loads",
                description = "Operations arranged for intracity logistics",
                iconResId = R.drawable.ic_delhivery_load
            )
        )

        loadsAdapter = ServiceConfigurationAdapter(
            services = services,
            onKnowMoreClick = { service ->
                Toast.makeText(this, "Know More: ${service.title}", Toast.LENGTH_SHORT).show()
                // Handle know more click
            },
            onConfigureClick = { service ->
                Toast.makeText(this, "Configure: ${service.title}", Toast.LENGTH_SHORT).show()
                // Handle configure click
            }
        )

        loadsRecyclerView.layoutManager = LinearLayoutManager(this)
        loadsRecyclerView.adapter = loadsAdapter
    }

    private fun setupFinancialRecyclerView() {
        financialRecyclerView = findViewById(R.id.financial_services_rv)

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

        financialAdapter = ServiceConfigurationAdapter(
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

        financialRecyclerView.layoutManager = LinearLayoutManager(this)
        financialRecyclerView.adapter = financialAdapter
    }

    private fun showFastagInfoBottomSheet() {
        FinancialServiceInfoBottomSheetDialogFragment.newInstance()
            .show(supportFragmentManager, "FinancialServiceInfoBottomSheet")
    }

    private fun setupBackButton() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressed()
        }
    }
}
