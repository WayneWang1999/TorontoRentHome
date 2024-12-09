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

        // -------- setup the auth variable
        firebaseAuth = Firebase.auth
        // Define the onFavoriteClick callback
        val onFavoriteClick: (House) -> Unit = { house ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser !=null) {
                // User is logged in, update the favorite list
                val favoriteHouseId = house.houseId

                // Add house ID to the current user's favorite list (this could be a list of house IDs)
                // Assuming you have a Firebase Firestore collection for users, where the user data is stored
                val userRef = FirebaseFirestore.getInstance().collection("tenants").document(currentUser.uid)
                userRef.update("favoriteHouseIds", FieldValue.arrayUnion(favoriteHouseId))
                // Show a success message
                Toast.makeText(context, "House added to favorites!", Toast.LENGTH_SHORT).show()
                // Update the view (in this case, you could refresh the adapter)
                houseAdapter.notifyDataSetChanged()
            } else {
                // If the user is not logged in, navigate to the AccountFragment
                Toast.makeText(context, "You need to log in to add favorites", Toast.LENGTH_SHORT).show()
                val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                bottomNavigationView.selectedItemId = R.id.miAccount
            }

            // Refresh the list to reflect the change
            houseAdapter.notifyDataSetChanged()
        }
        // Set up RecyclerView with an empty adapter initially
        houseAdapter = HouseAdapter(emptyList(),onFavoriteClick)
//        { house ->
//
//        }
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
}
