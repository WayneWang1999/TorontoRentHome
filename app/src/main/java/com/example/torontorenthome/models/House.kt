package com.example.torontorenthome.models

import com.example.torontorenthome.R

data class House @JvmOverloads constructor(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val description: String = "",
    val bedrooms: Int = 0,
    val bathrooms: Int = 2,
    val area: Int = 800,
    val price: Double = 0.0,
    val ownerId: String = "wwgtFtJ4LpeqsgECNtn6UTnHFUI3",
    val image: Int = R.drawable.house01,
    val type: String = "House",
    val createTime: String = "2 weeks"
)

