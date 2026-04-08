package com.delhivery.axle.ui.serviceConfigListing

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R

class ServiceConfigurationListActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceConfigurationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_configuration_list)
        
        setupRecyclerView()
        setupBackButton()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.configure_new_service_rv)
        
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
        
        adapter = ServiceConfigurationAdapter(
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
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupBackButton() {
        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }
}
