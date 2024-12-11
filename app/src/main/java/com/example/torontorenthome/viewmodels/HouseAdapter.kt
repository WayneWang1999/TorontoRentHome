package com.example.torontorenthome.viewmodels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.torontorenthome.R
import com.example.torontorenthome.databinding.BottomSheetHouseInfoBinding
import com.example.torontorenthome.models.House

class HouseAdapter(
    private var houses: List<House>,
    private val onFavoriteClick: (House) -> Unit,
    private var favoriteIds: Set<String>, // Pass favorite IDs
) : RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {

    class HouseViewHolder(private val binding: BottomSheetHouseInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(house: House, onFavoriteClick: (House) -> Unit, favoriteIds: Set<String>) {
            // Bind data to the views
            binding.tvPrice.text = "Price: $${house.price.toInt()}"
            binding.tvBedrooms.text = "BED: ${house.bedrooms} . BATH: ${house.bathrooms} . ${house.area} Ft"
            binding.tvDescription.text = "${house.type} . ${house.createTime} . ${house.address}"

            // Load image using Glide
            Glide.with(binding.root.context)
                .load(house.imageUrl)
                .placeholder(R.drawable.house01) // Placeholder while loading
                .into(binding.ivHouseImage)

            // Handle favorite click
            binding.imageFavorite.setOnClickListener { onFavoriteClick(house) }

            // Set the favorite icon based on whether the house is in favoriteIds
            if (favoriteIds.contains(house.houseId)) {
                binding.imageFavorite.setImageResource(R.drawable.ic_action_favorite_red)
            } else {
                binding.imageFavorite.setImageResource(R.drawable.ic_action_favorite)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val binding = BottomSheetHouseInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HouseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houses[position]
        holder.bind(house, onFavoriteClick, favoriteIds)
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
