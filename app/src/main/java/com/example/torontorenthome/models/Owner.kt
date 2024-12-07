package com.example.torontorenthome.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
@Entity(tableName = "owners")
data class Owner(
    @DocumentId
    @PrimaryKey
    val userId:String="",
    val uId:String="",
    val name:String="",
    val email:String="",
    val password:String="",
    val ownerHouseId:String=""
)
