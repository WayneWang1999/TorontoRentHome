package com.example.torontorenthome.util

import com.example.torontorenthome.R
import com.example.torontorenthome.models.House
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class HouseOperations {
    private val db = FirebaseFirestore.getInstance()

    fun generateRandomHousesAndUpload() {
        val batch = db.batch() // Use batch writes for efficiency
        val housesCollection = db.collection("houses")

        for (i in 1..50) {
            val latitude = Random.nextDouble(43.65, 43.78)
            val longitude = Random.nextDouble(-79.53, -79.33)
            // Generate random data
            val house = House(
                latitude = latitude,
                longitude = longitude,
                address = "House $i, Street ${Random.nextInt(1, 100)}, City",
                createTime = "${Random.nextInt(1, 100)} days",
                description = "A beautiful house with modern amenities.",
                bedrooms = Random.nextInt(1, 6),
                bathrooms = Random.nextInt(1,6),
                area = Random.nextInt(500,10000),
                price = Random.nextDouble(1000.0, 100000.0),
                image = R.drawable.house01

            )

            // Add to the batch
            val houseRef = housesCollection.document()
            batch.set(houseRef, house)
        }

        // Commit the batch
        batch.commit()
            .addOnSuccessListener {
                println("Successfully added 50 houses.")
            }
            .addOnFailureListener { e ->
                println("Error adding houses: ${e.message}")
            }
    }

    fun deleteAllHouses() {
        val housesCollection = db.collection("houses")

        housesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                for (document in querySnapshot.documents) {
                    batch.delete(document.reference)
                }
                // Commit the batch
                batch.commit()
                    .addOnSuccessListener {
                        println("Successfully deleted all houses.")
                    }
                    .addOnFailureListener { e ->
                        println("Error deleting houses: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                println("Error fetching documents: ${e.message}")
            }
    }
}