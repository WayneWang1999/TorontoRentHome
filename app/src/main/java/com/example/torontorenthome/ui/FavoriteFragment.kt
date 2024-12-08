package com.example.torontorenthome.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.torontorenthome.R
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.torontorenthome.models.House
import com.example.torontorenthome.viewmodels.HouseAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var houseAdapter: HouseAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        recyclerView = view.findViewById(R.id.rvFavoriteHouse)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize RecyclerView with an empty adapter
        houseAdapter = HouseAdapter(emptyList()) { house ->
            // Handle favorite click here, e.g., toggle favorite status
        }
        recyclerView.adapter = houseAdapter

        // Fetch favorite houses after checking if the user is authenticated
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            fetchFavoriteHouses(currentUser)
        } else {
            Toast.makeText(requireContext(), "Please log in to view favorites", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun fetchFavoriteHouses(user: FirebaseUser) {
        val userRef = firestore.collection("tenants").document(user.uid)

        // Fetch the user data to get their favorite house IDs
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteHouseIds = document.get("favoriteHouseIds") as? List<String>
                favoriteHouseIds?.let {
                    // Retrieve the houses based on the favoriteHouseIds
                    Toast.makeText(requireContext(), "there are ${favoriteHouseIds.size} haha", Toast.LENGTH_SHORT).show()
                    fetchHousesByIds(it)
                } ?: run {
                    Toast.makeText(requireContext(), "No favorite houses found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchHousesByIds(houseIds: List<String>) {
        val housesRef = firestore.collection("houses")

        // Fetch houses by their IDs
        Log.d("HouseIds", houseIds.toString())
        housesRef.whereIn(FieldPath.documentId(), houseIds).get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("FirestoreQuery", "Query result: ${querySnapshot.size()}") // Check number of results
                val houses = mutableListOf<House>()
                for (document in querySnapshot) {
                    val house = document.toObject(House::class.java)
                    houses.add(house)
                }
                Log.d("Firestore", "Fetched ${houses.size} houses")
                houseAdapter.updateData(houses)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching houses: ${e.message}")
                Toast.makeText(requireContext(), "Error fetching houses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
