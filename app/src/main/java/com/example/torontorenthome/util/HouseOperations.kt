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

        val imageUrls = listOf(
            "https://images.unsplash.com/photo-1480074568708-e7b720bb3f09?q=80&w=2074&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1554995207-c18c203602cb?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1560185007-cde436f6a4d0?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1675615667752-2ccda7042e7e?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1661962331652-514803c02b8a?q=80&w=1931&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://media.istockphoto.com/id/479767332/photo/idyllic-home-with-covered-porch.jpg?s=1024x1024&w=is&k=20&c=HfFAagx5qICx6X7GiskbsKoAEzkWb9tAwmW19D0q9m8=",
            "https://media.istockphoto.com/id/598165834/photo/nice-curb-appeal-of-american-craftsman-style-house.jpg?s=1024x1024&w=is&k=20&c=8UjUowY71dCmYvfGHi4k5Jjk8mRRHTdjPic3xt74tdM=",
            "https://media.istockphoto.com/id/590059304/photo/wooden-walkout-deck-well-kept-garden-with-bushes-and-flowers.jpg?s=1024x1024&w=is&k=20&c=-hoF2xsxOMmlRhOpmlId_gEAfOw7yNbffzWoPb6J7bE=",
            "https://media.istockphoto.com/id/481653068/photo/large-back-yard-with-greenery-and-furnished-porch.jpg?s=1024x1024&w=is&k=20&c=CNvqwJgqfbFZFEv3eA_y0khTjSMlM09f4AKpE2uhpT4=",
            "https://media.istockphoto.com/id/576931188/photo/exterior-of-luxury-house-with-grass-filled-back-yard.jpg?s=1024x1024&w=is&k=20&c=L4BgnHptK0zEPDtEnb0J08iD9yzhI_xOYc5o5ZwtR6c="
        )

        for (i in 1..100) {
            val latitude = Random.nextDouble(43.65, 43.78)
            val longitude = Random.nextDouble(-79.53, -79.33)
            val randomImageUrl = imageUrls.random()

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
                imageUrl = randomImageUrl

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