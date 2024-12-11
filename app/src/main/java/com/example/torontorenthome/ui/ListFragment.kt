package com.example.torontorenthome.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.torontorenthome.MyApp
import com.example.torontorenthome.R
import com.example.torontorenthome.data.HouseRepository
import com.example.torontorenthome.data.ListViewModelFactory
import com.example.torontorenthome.models.House
import com.example.torontorenthome.viewmodels.HouseAdapter
import com.example.torontorenthome.viewmodels.ListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var houseAdapter: HouseAdapter
    private lateinit var listViewModel: ListViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private val favoriteIds = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        recyclerView = view.findViewById(R.id.rvHouse)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize Repository and ViewModel
        val database = (requireContext().applicationContext as MyApp).database
        val houseDao = database.houseDao()
        val repository = HouseRepository(houseDao)
        val factory = ListViewModelFactory(repository)
        listViewModel = ViewModelProvider(this, factory).get(ListViewModel::class.java)

        // Initialize Firebase Auth
        firebaseAuth = Firebase.auth

        // Fetch and observe favorite IDs for the logged-in user
        fetchFavoriteIds()

        // Handle logout events
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                onLogout()
            }
        }

        // Define the onFavoriteClick callback
        val onFavoriteClick: (House) -> Unit = { house ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val favoriteHouseId = house.houseId
                val userRef = FirebaseFirestore.getInstance().collection("tenants").document(currentUser.uid)

                if (favoriteIds.contains(favoriteHouseId)) {
                    // Remove from favorites
                    userRef.update("favoriteHouseIds", FieldValue.arrayRemove(favoriteHouseId))
                    favoriteIds.remove(favoriteHouseId)
                    Toast.makeText(context, "House removed from favorites!", Toast.LENGTH_SHORT).show()
                } else {
                    // Add to favorites
                    userRef.update("favoriteHouseIds", FieldValue.arrayUnion(favoriteHouseId))
                    favoriteIds.add(favoriteHouseId)
                    Toast.makeText(context, "House added to favorites!", Toast.LENGTH_SHORT).show()
                }

                // Update the adapter with new favorite IDs
                houseAdapter.updateFavoriteIds(favoriteIds)
            } else {
                // Navigate to AccountFragment if not logged in
                Toast.makeText(context, "You need to log in to add favorites", Toast.LENGTH_SHORT).show()
                val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                bottomNavigationView.selectedItemId = R.id.miAccount
            }
        }

        // Set up RecyclerView with an empty adapter initially
        houseAdapter = HouseAdapter(emptyList(), onFavoriteClick, favoriteIds)
        recyclerView.adapter = houseAdapter

        // Observe houses LiveData
        listViewModel.houses.observe(viewLifecycleOwner) { houses ->
            if (houses != null) {
                // Update the adapter with new data
                houseAdapter.updateData(houses)
            }
        }

        // Fetch houses from ViewModel
        listViewModel.fetchHouses()

        return view
    }

    private fun fetchFavoriteIds() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userRef = FirebaseFirestore.getInstance().collection("tenants").document(currentUser.uid)
            userRef.get().addOnSuccessListener { document ->
                val favoriteHouseIds = document.get("favoriteHouseIds") as? List<String>
                if (favoriteHouseIds != null) {
                    favoriteIds.clear()
                    favoriteIds.addAll(favoriteHouseIds)
                    houseAdapter.updateFavoriteIds(favoriteIds)
                }
            }
        }
    }

    private fun onLogout() {
        // Clear favorite IDs
        favoriteIds.clear()

        // Notify the adapter about the change
        houseAdapter.updateFavoriteIds(favoriteIds)


        val context = context // context is nullable
        if (context != null) {
            Toast.makeText(context, "User is logout now!!!", Toast.LENGTH_SHORT).show()
        } else {
            // Log or handle the case where context is null
        }
    // Optionally show a message  This make the app crush  ******************
     //   firebaseAuth.addAuthStateListener   this listener when the AccountFragment do the logout
        // it action and call this function. now this fragment dis attached to the activity . the context
        //will be null, it will make the system crush.***********************
       //Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
    }
}
