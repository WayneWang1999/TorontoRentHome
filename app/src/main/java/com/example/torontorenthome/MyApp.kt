package com.example.torontorenthome

import android.app.Application
import androidx.room.Room
import com.example.torontorenthome.data.HouseDatabase

class MyApp : Application() {
    lateinit var database: HouseDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            HouseDatabase::class.java,
            "app_database"
        ).build()
    }
}