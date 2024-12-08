package com.example.torontorenthome.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.torontorenthome.models.House

@Dao
interface HouseDao {
    @Query("SELECT * FROM houses")
    suspend fun getAllHouses(): List<House>

    @Query("SELECT * FROM houses WHERE houseId = :houseId")
    suspend fun getHouseById(houseId: String): House?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouses(houses: List<House>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouse(house: House)

//    @Query("DELETE FROM houses")
//    suspend fun clearTable()
}