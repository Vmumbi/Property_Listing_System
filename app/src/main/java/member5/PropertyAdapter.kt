package com.example.property_listing_system.member5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.property_listing_system.R
import com.example.property_listing_system.data.local.entity.PropertyEntity

class PropertyAdapter(
    private val onItemClick: (PropertyEntity) -> Unit
) : ListAdapter<PropertyEntity, PropertyAdapter.PropertyViewHolder>(DIFF_CALLBACK) {

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = getItem(position)

        holder.tvTitle.text = property.title
        holder.tvPrice.text = "${property.currency} ${"%,.0f".format(property.price)}"
        holder.tvLocation.text = "${property.address}, ${property.city}"
        holder.tvDetails.text = "${property.propertyType} · ${property.bedrooms} bed · ${property.bathrooms} bath"
        holder.tvStatus.text = property.status

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