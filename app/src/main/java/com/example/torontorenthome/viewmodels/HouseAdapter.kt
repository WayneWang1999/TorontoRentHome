package com.example.torontorenthome.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.torontorenthome.R
import com.example.torontorenthome.models.House

class HouseAdapter(
    private var houses: List<House>,
    private val onFavoriteClick: (House) -> Unit,
    private var favoriteIds: Set<String>, // Pass favorite IDs
) : RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {

    class HouseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageUrl: ImageView = view.findViewById(R.id.ivHouseImage)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val bedrooms: TextView = view.findViewById(R.id.tvBedrooms)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val favorite: ImageView = view.findViewById(R.id.imageFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bottom_sheet_house_info, parent, false)
        return HouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houses[position]

        // Bind data to the views

        holder.price.text = "Price: $${house.price.toInt()}"
        holder.bedrooms.text =
            "BED: ${house.bedrooms} . BATH:${house.bathrooms} . ${house.area}  Ft"
        holder.description.text = "${house.type}   . ${house.createTime}"
        // Load image using a library like Glide or Coil
        Glide.with(holder.itemView.context)
            .load(house.imageUrl) // Load the picture from URL
            .placeholder(R.drawable.house01) // Placeholder while loading
            .into(holder.imageUrl)
        // Handle favorite click
        holder.favorite.setOnClickListener { onFavoriteClick(house) }

        // Set the favorite icon based on whether the house is in favoriteIds
        if (favoriteIds.contains(house.houseId)) {
            holder.favorite.setImageResource(R.drawable.ic_action_favorite_red)
        } else {
           holder.favorite.setImageResource(R.drawable.ic_action_favorite)
        }
    }

    override fun getItemCount(): Int = houses.size

    // Update data dynamically
    fun updateData(newHouses: List<House>) {
        houses = newHouses
        notifyDataSetChanged()
    }

    fun updateFavoriteIds(newFavoriteIds: Set<String>) {
        favoriteIds = newFavoriteIds
        notifyDataSetChanged()
    }
}