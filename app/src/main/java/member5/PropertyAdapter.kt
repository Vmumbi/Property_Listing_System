package com.example.property_listing_system.member5

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.property_listing_system.R
import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.location.MapIntentHelper

class PropertyAdapter(
    private val onItemClick: (PropertyEntity) -> Unit
) : ListAdapter<PropertyEntity, PropertyAdapter.PropertyViewHolder>(DIFF_CALLBACK) {

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPropertyImage: ImageView = itemView.findViewById(R.id.ivPropertyImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvContactPhone: TextView = itemView.findViewById(R.id.tvContactPhone)
        val tvContactEmail: TextView = itemView.findViewById(R.id.tvContactEmail)
        val btnCallAgent: Button = itemView.findViewById(R.id.btnCallAgent)
        val btnViewMap: Button = itemView.findViewById(R.id.btnViewMap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = getItem(position)
        val context = holder.itemView.context

        holder.tvTitle.text = property.title

        // Format Price with Monthly / Yearly / Total option
        val formattedNumber = "%,.0f".format(property.price)
        holder.tvPrice.text = when (property.pricePeriod) {
            PropertyEntity.PERIOD_MONTHLY -> "${property.currency} $formattedNumber / Month"
            PropertyEntity.PERIOD_YEARLY -> "${property.currency} $formattedNumber / Year"
            else -> "${property.currency} $formattedNumber Total"
        }

        holder.tvLocation.text = "${property.address}, ${property.city}"
        holder.tvDetails.text = "${property.propertyType} • ${property.bedrooms} Beds • ${property.bathrooms} Baths • ${property.squareFootage.toInt()} sq ft"
        holder.tvStatus.text = property.status

        holder.tvContactPhone.text = "📞 Phone: ${property.contactPhone.ifBlank { "+254 700 000 000" }}"
        holder.tvContactEmail.text = "✉️ Email: ${property.contactEmail.ifBlank { "agent@realtor.co.ke" }}"

        // Call Agent Action
        holder.btnCallAgent.setOnClickListener {
            val phone = property.contactPhone.ifBlank { "0712345678" }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { context.startActivity(intent) }
        }

        // View on Map Action
        holder.btnViewMap.setOnClickListener {
            MapIntentHelper.openPropertyInMaps(
                context = context,
                latitude = property.latitude,
                longitude = property.longitude,
                propertyTitle = property.title
            )
        }

        holder.itemView.setOnClickListener { onItemClick(property) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PropertyEntity>() {
            override fun areItemsTheSame(oldItem: PropertyEntity, newItem: PropertyEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PropertyEntity, newItem: PropertyEntity) =
                oldItem == newItem
        }
    }
}
