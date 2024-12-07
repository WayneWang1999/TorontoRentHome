package com.example.torontorenthome.data

import com.example.torontorenthome.models.House
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class HouseRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchHouses(): List<House> {
        return try {
            db.collection("houses").get().await().documents.mapNotNull { it.toObject(House::class.java) }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchHouseDetails(houseId: String): House? {
        return try {
            db.collection("houses").document(houseId).get().await().toObject(House::class.java)
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }
}
