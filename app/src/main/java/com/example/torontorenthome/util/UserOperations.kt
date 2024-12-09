package com.example.torontorenthome.util

import com.example.torontorenthome.models.Owner
import com.example.torontorenthome.models.Tenant
import com.google.firebase.firestore.FirebaseFirestore

class UserOperations {
    private val db = FirebaseFirestore.getInstance()

    fun generateAllUsersAndUpload(){
        generateTenantsAndUpload()
        generateOwnersAndUpload()
    }

    private fun generateOwnersAndUpload() {
        val batch = db.batch() // Use batch writes for efficiency
        val ownersCollection = db.collection("owners")

        for (i in 10..20) {
             // Generate random data
            val owner = Owner(
                name="owner$i",
                email="owner$i@gmail.com",
                password="111111",
                ownerHouseIds= listOf()
               )

            // Add to the batch
            val ownerRef = ownersCollection.document()
            batch.set(ownerRef, owner)
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

    private fun generateTenantsAndUpload() {
        val batch = db.batch() // Use batch writes for efficiency
        val ownersCollection = db.collection("tenants")

        for (i in 10..20) {
            // Generate random data
            val tenant = Tenant(
                name="tenant$i",
                email="tenant$i@gmail.com",
                password="111111",
                favoriteHouseIds = listOf(),
            )

            // Add to the batch
            val tenantRef = ownersCollection.document()
            batch.set(tenantRef, tenant)
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

    fun deleteAllUsers(){
        deleteAllOwners()
        deleteAllTenants()
    }

    private fun deleteAllOwners() {
        val ownersCollection = db.collection("owners")

        ownersCollection.get()
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

    private fun deleteAllTenants() {
        val tenantsCollection = db.collection("tenants")

        tenantsCollection.get()
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