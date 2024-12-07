package com.example.torontorenthome.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.torontorenthome.models.House

@Database(entities = [House::class], version = 1)
abstract class HouseDatabase : RoomDatabase() {
    abstract fun houseDao(): HouseDao
}