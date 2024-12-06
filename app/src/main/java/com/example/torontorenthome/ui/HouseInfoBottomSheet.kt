package com.example.torontorenthome.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.torontorenthome.databinding.BottomSheetHouseInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HouseInfoBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetHouseInfoBinding? = null
    private val binding get() = _binding!!

    private var houseImage: Int = 0
    private var description: String = ""
    private var type: String = ""
    private var createTime: String = ""
    private var bedrooms: Int = 0
    private var price: Double = 0.0
    private var bathrooms: Int = 0
    private var area: Int = 0

    companion object {
        fun newInstance(
            image: Int,
            description: String,
            type: String,
            createTime: String,
            bedrooms: Int,
            price: Double,
            bathrooms: Int,
            area: Int
        ): HouseInfoBottomSheet {
            val fragment = HouseInfoBottomSheet()
            val args = Bundle()
            args.putInt("houseImage", image)
            args.putString("description", description)
            args.putString("type", type)
            args.putString("createTime", createTime)
            args.putInt("bedrooms", bedrooms)
            args.putInt("bathrooms", bathrooms)
            args.putInt("area", area)
            args.putDouble("price", price)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            houseImage = it.getInt("houseImage", 0)
            description = it.getString("description", "")
            type = it.getString("type", "")
            createTime = it.getString("createTime", "")
            bedrooms = it.getInt("bedrooms", 0)
            price = it.getDouble("price", 0.0)
            bathrooms = it.getInt("bathrooms", 0)
            area = it.getInt("area", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHouseInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set data using View Binding
        binding.ivHouseImage.setImageResource(houseImage)
        binding.tvPrice.text = "Price: $${price.toInt()}"
        binding.tvBedrooms.text = "BED: $bedrooms . BATH: $bathrooms . $area Ft"
        binding.tvDescription.text = "$type   .   $createTime"
        binding.imageFavorite.setOnClickListener {
            // Add this house to the currentUser Profile (Mock action for now)
            Toast.makeText(requireContext(), "Add the house to Favorite", Toast.LENGTH_LONG).show()
        }
    }
}
