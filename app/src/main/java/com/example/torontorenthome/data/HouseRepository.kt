package com.example.torontorenthome.data

import com.example.torontorenthome.models.House
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class HouseRepository (private val houseDao: HouseDao){

    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchHouses(): List<House> {
        return try {
            // Fetch from FireStore
            val houses = db.collection("houses")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(House::class.java) }

            // Save to local database
            houseDao.insertHouses(houses)

            // Return fetched data
            houses
        } catch (e: Exception) {
            // Fetch from Room database on failure
            e.printStackTrace() // Log the error for debugging
            houseDao.getAllHouses()
        }
    }


    // Fetch house details by ID
    suspend fun fetchHouseDetails(houseId: String): House? {
        return try {
            val house = db.collection("houses").document(houseId).get().await()
                .toObject(House::class.java)
            house?.let {
                houseDao.insertHouse(it) // Save to local database
            }
            house
        } catch (e: FirebaseFirestoreException) {
            // Fetch from Room database on failure
            houseDao.getHouseById(houseId)
        }
    }
}
