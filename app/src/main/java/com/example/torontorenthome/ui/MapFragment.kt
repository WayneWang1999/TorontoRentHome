package com.example.torontorenthome.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
        setupMapView(savedInstanceState)
        initializeViewModel()
        setupSearchView()
        setupAppNameClickListener()
        setupFilterClickListener()
    }

    private fun setupMapView(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    private fun initializeViewModel() {
        val repository = HouseRepository((requireContext().applicationContext as MyApp).database.houseDao())
        mapViewModel = ViewModelProvider(this, MapViewModelFactory(repository)).get(MapViewModel::class.java)

        // Observe all houses (only triggered when fetching all houses)
        mapViewModel.houses.observe(viewLifecycleOwner) { houses ->
            houses?.let { addMarkersToMap(it) }        }

        // Observe house details for specific queries
        mapViewModel.houseDetails.observe(viewLifecycleOwner) { house ->
            house?.let { showHouseInfoBottomSheet(it) }
        }

        mapViewModel.fetchHouses()
    }

    private fun setupSearchView() {
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                binding.svSearch.clearFocus()
                hideKeyboard(binding.svSearch)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optionally handle text change if needed
                return true
            }
        })
    }

    private fun setupAppNameClickListener() {
        binding.tvAppName.setOnClickListener {
            HouseOperations().generateRandomHousesAndUpload()
            Toast.makeText(requireContext(), "App Name Clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFilterClickListener() {
        binding.imageFilter.setOnClickListener {
            // HouseOperations().deleteAllHouses() // Uncomment when needed
            Toast.makeText(requireContext(), "Filter Clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSearch(query: String) {
        Log.d("Search", "performSearch called with query: $query")
        if (query.isNotEmpty()) {
            val price = query.toDoubleOrNull()
            if (price != null) {
                val filteredHouses = mapViewModel.houses.value?.filter { it.price > price }
                filteredHouses?.let {
                    googleMap.clear()
                    addMarkersToMap(it)
                    Toast.makeText(context, "Showing houses priced above $$price", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(context, "No houses found above $$price", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarkersToMap(houses: List<House>) {
        Log.d("Search", "addMarkersToMap is called")
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
        houseId?.let { id ->
            // Avoid redundant fetch or re-observation
            if (mapViewModel.houseDetails.value?.houseId != id) {
                mapViewModel.fetchHouseDetails(id)
            }
        }
        true
    }
}

    private fun showHouseInfoBottomSheet(house: House) {
        Log.d("Search", "showHouseInfoBottomSheet is called")
        val existingBottomSheet = parentFragmentManager.findFragmentByTag("HouseInfoBottomSheet")
        if (existingBottomSheet != null) {
            (existingBottomSheet as? HouseInfoBottomSheet)?.dismiss()
        }

        val bottomSheet = HouseInfoBottomSheet.newInstance(
            house.imageUrl, house.description, house.type, house.createTime,
            house.bedrooms, house.price, house.bathrooms, house.area, house.address
        )
        bottomSheet.show(parentFragmentManager, "HouseInfoBottomSheet")
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun enableUserLocation() {
        if (areLocationPermissionsGranted()) {
            try {
                googleMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Toast.makeText(
                    requireContext(),
                    "Error enabling location: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            requestLocationPermissions()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (areLocationPermissionsGranted()) {
            enableUserLocation()
        }

        val defaultLocation = LatLng(43.677308, -79.406927)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            enableUserLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

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
