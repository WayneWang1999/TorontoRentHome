package com.example.torontorenthome.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.torontorenthome.models.House
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()


    private val _houses = MutableLiveData<List<House>>()
    val houses: LiveData<List<House>> get() = _houses
    // Fetch houses from Firestore
    fun fetchHouses() {
        db.collection("houses").get()
            .addOnSuccessListener { querySnapshot ->
                val houseList = querySnapshot.documents.mapNotNull { it.toObject(House::class.java) }
                _houses.postValue(houseList)
            }
            .addOnFailureListener { e ->
                // Handle the failure case, maybe post an error message
                Log.e("MapViewModel", "Error fetching houses: ${e.message}")
            }
    }

    private val _houseDetails = MutableLiveData<House>()
    val houseDetails: LiveData<House> get() = _houseDetails

    fun fetchHouseDetails(houseId: String) {
        db.collection("houses").document(houseId).get()
            .addOnSuccessListener { documentSnapshot ->
                val house = documentSnapshot.toObject(House::class.java)
                _houseDetails.value = house
            }
            .addOnFailureListener { e ->
                // Handle error, maybe post a failure state
                Log.e("MapViewModel", "Error fetching house details", e)
            }
    }
}