package com.example.torontorenthome.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.torontorenthome.data.HouseRepository
import com.example.torontorenthome.data.MapViewModelFactory
import com.example.torontorenthome.databinding.FragmentMapBinding
import com.example.torontorenthome.models.House
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
        val repository = HouseRepository()
        val factory = MapViewModelFactory(repository)
        mapViewModel = ViewModelProvider(this, factory).get(MapViewModel::class.java)

        // Observe houses LiveData
        mapViewModel.houses.observe(viewLifecycleOwner) { houses ->
            houses?.let { addMarkersToMap(it) }
        }

        // Fetch houses from ViewModel
        mapViewModel.fetchHouses()

        // Setup interactions
        binding.tvAppName.setOnClickListener {
            Toast.makeText(requireContext(), "App Name Clicked!", Toast.LENGTH_SHORT).show()
        }

        binding.imageFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Filter Clicked!", Toast.LENGTH_SHORT).show()
        }
    }

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
        // Show house details in BottomSheet
        val bottomSheet = HouseInfoBottomSheet.newInstance(
            house.image,
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
