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
import com.example.torontorenthome.databinding.FragmentMapBinding
import com.example.torontorenthome.models.House
import com.example.torontorenthome.util.HouseOperations
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val COLLECTION_HOUSES = "houses"
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: com.google.android.gms.maps.MapView
    private lateinit var googleMap: GoogleMap

    private val houseOperations = HouseOperations()  // House operations to generate/upload houses

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.tvAppName.setOnClickListener {
            houseOperations.generateRandomHousesAndUpload() // Generate and upload houses
        }

        binding.imageFilter.setOnClickListener {
            houseOperations.deleteAllHouses() // Delete all houses
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Request location permissions
        if (areLocationPermissionsGranted()) {
            enableUserLocation()
        } else {
            requestLocationPermissions()
        }

        // Set default map position
        val defaultLocation = LatLng(43.677308, -79.406927) // Default location (GeorgeBrown)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Enable zoom controls
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Add a default marker
        googleMap.addMarker(
            MarkerOptions()
                .position(defaultLocation)
                .title("GeorgeBrown")
        )

        // Add house markers to the map
        addMarkersToMap()
    }

    // Check if fine location permission is granted
    private fun isFineLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Check if coarse location permission is granted
    private fun isCoarseLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Check if both location permissions are granted
    private fun areLocationPermissionsGranted(): Boolean {
        return isFineLocationPermissionGranted() && isCoarseLocationPermissionGranted()
    }

    // Request location permissions
    private fun requestLocationPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Enable the user's location on the map
    private fun enableUserLocation() {
        if (isFineLocationPermissionGranted()) {
            googleMap.isMyLocationEnabled = true
        }
    }

    // Handle the results of permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val fineLocationGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val coarseLocationGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (fineLocationGranted && coarseLocationGranted) {
                        enableUserLocation() // Permissions granted
                    } else {
                        // Show a message if any permission was denied
                        val deniedPermissions = mutableListOf<String>()
                        if (!fineLocationGranted) deniedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                        if (!coarseLocationGranted) deniedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

                        Toast.makeText(
                            requireContext(),
                            "Permission(s) denied: ${deniedPermissions.joinToString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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

    // Add house markers to the map
    private fun addMarkersToMap() {
        db.collection(COLLECTION_HOUSES).get()
            .addOnSuccessListener { querySnapshot ->
                val houses = querySnapshot.documents.mapNotNull { it.toObject(House::class.java) }
                houses.forEach { house ->
                    val location = LatLng(house.latitude, house.longitude)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(house.description)
                    )?.tag = house
                }
                setupMarkerClickListener()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching houses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Setup marker click listener to show house details
    private fun setupMarkerClickListener() {
        googleMap.setOnMarkerClickListener { marker ->
            (marker.tag as? House)?.let { house ->
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
            true
        }
    }
}
