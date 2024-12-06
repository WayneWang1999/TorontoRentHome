package com.example.torontorenthome.models

data class HouseInfo(
    val image: Int,
    val description: String,
    val type: String,
    val createTime: String,
    val bedrooms: Int,
    val price: Double,
    val bathrooms:Int,
    val area:Int,
)