package com.example.torontorenthome.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "tenants")
data class Tenant(
    @DocumentId
    @PrimaryKey
    val userId:String="",
    val uId:String="",
    val name:String="",
    val email:String="",
    val password:String="",
    val favoriteHouseId:String="",

)
