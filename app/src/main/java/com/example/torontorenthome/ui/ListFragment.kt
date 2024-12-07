package com.example.torontorenthome.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.torontorenthome.MyApp
import com.example.torontorenthome.R
import com.example.torontorenthome.data.HouseRepository
import com.example.torontorenthome.data.ListViewModelFactory
import com.example.torontorenthome.viewmodels.HouseAdapter
import com.example.torontorenthome.viewmodels.ListViewModel

class ListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var houseAdapter: HouseAdapter
    private lateinit var listViewModel: ListViewModel

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

        // Set up RecyclerView with an empty adapter initially
        houseAdapter = HouseAdapter(emptyList()) { house ->
            // Handle favorite click here
        }
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
