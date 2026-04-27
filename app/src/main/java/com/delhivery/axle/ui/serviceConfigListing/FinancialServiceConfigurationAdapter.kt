package com.delhivery.axle.ui.serviceConfigListing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.databinding.ServiceConfigurationViewBinding

class FinancialServiceConfigurationAdapter(
    private val services: List<ServiceConfigurationModel>,
    private val onKnowMoreClick: (ServiceConfigurationModel) -> Unit,
    private val onConfigureClick: (ServiceConfigurationModel) -> Unit
) : RecyclerView.Adapter<FinancialServiceConfigurationAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(private val binding: ServiceConfigurationViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: ServiceConfigurationModel) {
            binding.apply {
                tvServiceTitle.text = service.title
                tvServiceDescription.text = service.description
                ivServiceIcon.setImageResource(service.iconResId)

                if (service.status != null) {
                    statusBanner.visibility = View.VISIBLE
                    tvStatusTitle.text = service.status.statusText
                } else {
                    statusBanner.visibility = View.GONE
                }

                btnKnowMore.setOnClickListener {
                    onKnowMoreClick(service)
                }

                btnConfigure.setOnClickListener {
                    onConfigureClick(service)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ServiceConfigurationViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount(): Int = services.size
}
