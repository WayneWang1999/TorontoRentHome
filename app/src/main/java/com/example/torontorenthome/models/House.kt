package com.example.torontorenthome.models

data class House(
    val latitude: Double,
    val longitude:Double,
    val address: String,
    val description: String,
    val bedrooms: Int,
    val bathrooms:Int=2,
    val area:Int=800,
    val price: Double,
    val ownerId:String="wwgtFtJ4LpeqsgECNtn6UTnHFUI3",
    val image: Int,
    val type:String="House",
    val createTime:String="2 weeks",
)
