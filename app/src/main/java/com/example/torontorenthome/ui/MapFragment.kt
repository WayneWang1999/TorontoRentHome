package com.example.torontorenthome.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.torontorenthome.MyApp
import com.example.torontorenthome.data.HouseRepository
import com.example.torontorenthome.data.MapViewModelFactory
import com.example.torontorenthome.databinding.FragmentMapBinding
import com.example.torontorenthome.models.House
import com.example.torontorenthome.util.HouseOperations
import com.example.torontorenthome.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var mapViewModel: MapViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize MapView
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize Repository and ViewModel
        val database = (requireContext().applicationContext as MyApp).database
        val houseDao = database.houseDao()
        val repository = HouseRepository(houseDao)
        val factory = MapViewModelFactory(repository)
        mapViewModel = ViewModelProvider(this, factory).get(MapViewModel::class.java)

        // Observe houses LiveData
        mapViewModel.houses.observe(viewLifecycleOwner) { houses ->
            houses?.let { addMarkersToMap(it) }
        }

        // Fetch houses from ViewModel
        mapViewModel.fetchHouses()

        // Setup interactions
        val houseOperations=HouseOperations()
        binding.tvAppName.setOnClickListener {
        //    houseOperations.generateRandomHousesAndUpload()
            Toast.makeText(requireContext(), "App Name Clicked!", Toast.LENGTH_SHORT).show()
        }

        binding.imageFilter.setOnClickListener {
         //   houseOperations.deleteAllHouses()
            Toast.makeText(requireContext(), "Filter Clicked!", Toast.LENGTH_SHORT).show()
        }

        // Set up the listener for search query text changes
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform the search with the submitted query
                query?.let {
                    performSearch(it)
                }
                binding.svSearch.clearFocus() // Clear focus from SearchView
               hideKeyboard(binding.svSearch) // Hide the keyboard
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Update search results dynamically as the user types
                newText?.let {
                   // updateSearchResults(it)
                }
                return true
            }
        })
    }
    fun hideKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun performSearch(query: String) {
        // Check if the query is not empty or null before performing a search
        if (query.isNotEmpty()) {
            val price = query.toDoubleOrNull()
            if (price != null) {
                // Filter houses with price greater than the entered value
                val filteredHouses = mapViewModel.houses.value?.filter { it.price > price }
                if (filteredHouses != null) {
                    googleMap.clear() // Clear existing markers
                    addMarkersToMap(filteredHouses) // Add filtered markers
                    Toast.makeText(context, "Showing houses priced above $$price", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No houses found above $$price", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

      }


//    private fun updateSearchResults(newText: String) {
//        Toast.makeText(context,"updateSerachResults",Toast.LENGTH_SHORT).show()
//        // Update the UI with filtered results based on the current text
//    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (areLocationPermissionsGranted()) {
            enableUserLocation()
        } else {
            requestLocationPermissions()
        }

        val defaultLocation = LatLng(43.677308, -79.406927) // Default location (e.g., GeorgeBrown)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    private fun addMarkersToMap(houses: List<House>) {
        houses.forEach { house ->
            val location = LatLng(house.latitude, house.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(house.description)
            )?.tag = house.houseId
        }

        setupMarkerClickListener()
    }

    private fun setupMarkerClickListener() {
        googleMap.setOnMarkerClickListener { marker ->
            val houseId = marker.tag as? String
            houseId?.let { mapViewModel.fetchHouseDetails(it) }
            true
        }

        mapViewModel.houseDetails.observe(viewLifecycleOwner) { house ->
            house?.let {
                showHouseInfoBottomSheet(house)
                // Show house details (e.g., BottomSheet or dialog)
            }
        }
    }
    private fun showHouseInfoBottomSheet(house: House) {
        // Check if the BottomSheet is already shown
        val existingBottomSheet = parentFragmentManager.findFragmentByTag("HouseInfoBottomSheet")

        if (existingBottomSheet != null) {
            // Dismiss the existing BottomSheet if it's shown
            (existingBottomSheet as? HouseInfoBottomSheet)?.dismiss()
        }

        // Show a new BottomSheet
        val bottomSheet = HouseInfoBottomSheet.newInstance(
            house.imageUrl,
            house.description,
            house.type,
            house.createTime,
            house.bedrooms,
            house.price,
            house.bathrooms,
            house.area
        )
        bottomSheet.show(parentFragmentManager, "HouseInfoBottomSheet")
    }


    // Permission helpers
    private fun isFineLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isCoarseLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return isFineLocationPermissionGranted() && isCoarseLocationPermissionGranted()
    }

    private fun requestLocationPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun enableUserLocation() {
        if (isFineLocationPermissionGranted()) {
            try {
                googleMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Error enabling location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                enableUserLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lifecycle methods for MapView
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
