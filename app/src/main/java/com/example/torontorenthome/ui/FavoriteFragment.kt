package com.example.torontorenthome.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.torontorenthome.R
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.torontorenthome.data.FavoriteViewModelFactory
import com.example.torontorenthome.viewmodels.FavoriteFragmentViewModel
import com.example.torontorenthome.viewmodels.HouseAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var houseAdapter: HouseAdapter
    private lateinit var viewModel: FavoriteFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        recyclerView = view.findViewById(R.id.rvFavoriteHouse)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        houseAdapter = HouseAdapter(emptyList()) { house ->
            // Handle favorite click
        }
        recyclerView.adapter = houseAdapter

        // Initialize the ViewModel with the factory
        val factory = FavoriteViewModelFactory(
            firestore = FirebaseFirestore.getInstance(),
            firebaseAuth = FirebaseAuth.getInstance()
        )
        viewModel = ViewModelProvider(this, factory)[FavoriteFragmentViewModel::class.java]

        observeViewModel()

        // Trigger user check
        viewModel.checkUserAuthentication()
        // Trigger data fetch
       // viewModel.fetchFavoriteHouses()

        return view
    }

    private fun observeViewModel() {
        viewModel.favoriteHouses.observe(viewLifecycleOwner) { houses ->
            houseAdapter.updateData(houses)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.navigateToAccount.observe(viewLifecycleOwner) { shouldNavigate ->
            Log.d("shouldNavigat","$shouldNavigate")
            if (shouldNavigate) {
                navigateToAccountFragment()
            }
        }
    }

    private fun navigateToAccountFragment() {
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.miAccount

    }
}
