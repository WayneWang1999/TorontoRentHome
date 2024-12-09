package com.example.torontorenthome.ui

import FavoriteFragmentViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.torontorenthome.R
import com.example.torontorenthome.data.FavoriteViewModelFactory
import com.example.torontorenthome.models.House
import com.example.torontorenthome.viewmodels.HouseAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var houseAdapter: HouseAdapter
    private lateinit var viewModel: FavoriteFragmentViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private val favoriteIds = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        recyclerView = view.findViewById(R.id.rvFavoriteHouse)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize Firebase Auth
        firebaseAuth = Firebase.auth

        // Set up RecyclerView and adapter with an empty list initially
        val onFavoriteClick: (House) -> Unit = { house ->
            handleFavoriteClick(house)
        }
        houseAdapter = HouseAdapter(emptyList(), onFavoriteClick, favoriteIds)
        recyclerView.adapter = houseAdapter

        // Initialize the ViewModel with the factory
        val factory = FavoriteViewModelFactory(
            firestore = FirebaseFirestore.getInstance(),
            firebaseAuth = FirebaseAuth.getInstance()
        )
        viewModel = ViewModelProvider(this, factory)[FavoriteFragmentViewModel::class.java]

        observeViewModel()

        // Check user authentication and fetch favorite IDs
        viewModel.checkUserAuthentication()

        return view
    }

    private fun observeViewModel() {
        viewModel.favoriteIds.observe(viewLifecycleOwner) { ids ->
            // Update the favoriteIds set and notify the adapter
            favoriteIds.clear()
            favoriteIds.addAll(ids)
            houseAdapter.updateFavoriteIds(favoriteIds)
        }

        viewModel.favoriteHouses.observe(viewLifecycleOwner) { houses ->
            // Update the adapter with the list of houses
            houseAdapter.updateData(houses)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.navigateToAccount.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                navigateToAccountFragment()
            }
        }
    }

    private fun handleFavoriteClick(house: House) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val favoriteHouseId = house.houseId
            val userRef = FirebaseFirestore.getInstance().collection("tenants").document(currentUser.uid)

            if (favoriteIds.contains(favoriteHouseId)) {
                // Remove from favorites
                userRef.update("favoriteHouseIds", FieldValue.arrayRemove(favoriteHouseId))
                    .addOnSuccessListener {
                        favoriteIds.remove(favoriteHouseId)
                        Toast.makeText(context, "House removed from favorites!", Toast.LENGTH_SHORT).show()
                        // Refresh the page by re-fetching the data
                        viewModel.fetchFavoriteHouses(currentUser.uid)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to remove favorite: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Add to favorites
                userRef.update("favoriteHouseIds", FieldValue.arrayUnion(favoriteHouseId))
                    .addOnSuccessListener {
                        favoriteIds.add(favoriteHouseId)
                        Toast.makeText(context, "House added to favorites!", Toast.LENGTH_SHORT).show()
                        // Refresh the page by re-fetching the data
                        viewModel.fetchFavoriteHouses(currentUser.uid)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to add favorite: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            // Navigate to AccountFragment if not logged in
            Toast.makeText(context, "You need to log in to add favorites", Toast.LENGTH_SHORT).show()
            navigateToAccountFragment()
        }
    }


    private fun navigateToAccountFragment() {
        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.miAccount
    }
}
