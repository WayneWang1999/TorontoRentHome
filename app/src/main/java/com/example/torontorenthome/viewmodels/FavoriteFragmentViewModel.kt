package com.example.torontorenthome.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.torontorenthome.models.House
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteFragmentViewModel(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _favoriteHouses = MutableLiveData<List<House>>()
    val favoriteHouses: LiveData<List<House>> get() = _favoriteHouses

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _navigateToAccount = MutableLiveData<Boolean>()
    val navigateToAccount: LiveData<Boolean> get() = _navigateToAccount

    fun checkUserAuthentication() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            _navigateToAccount.value = true
        } else {
            fetchFavoriteHouses(currentUser.uid)
        }
    }

    private fun fetchFavoriteHouses(userId: String) {
        val userRef = firestore.collection("tenants").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteHouseIds = document.get("favoriteHouseIds") as? List<String>
                    if (favoriteHouseIds.isNullOrEmpty()) {
                        _error.value = "No favorite houses found"
                        _favoriteHouses.value = emptyList()
                    } else {
                        fetchHousesByIds(favoriteHouseIds)
                    }
                } else {
                    _error.value = "User data not found"
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Error fetching user data: ${e.message}"
            }
    }

    private fun fetchHousesByIds(houseIds: List<String>) {
        val housesRef = firestore.collection("houses")
        housesRef.whereIn(FieldPath.documentId(), houseIds).get()
            .addOnSuccessListener { querySnapshot ->
                val houses = querySnapshot.map { it.toObject(House::class.java) }
                _favoriteHouses.value = houses
            }
            .addOnFailureListener { e ->
                _error.value = "Error fetching houses: ${e.message}"
            }
    }
}