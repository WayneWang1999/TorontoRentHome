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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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

    private val houseOperations = HouseOperations()  // House operations to generate/upload houses data


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize ViewModel
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        // Observe the houses LiveData
        mapViewModel.houses.observe(viewLifecycleOwner, Observer { houses ->
            houses?.let {
                addMarkersToMap(it)
            }
        })
       // Observe the house details LiveData
        mapViewModel.houseDetails.observe(viewLifecycleOwner, Observer { house ->
            house?.let {
                showHouseInfoBottomSheet(it)
            }
        })


        // Fetch houses when the fragment is ready
        mapViewModel.fetchHouses()

        binding.tvAppName.setOnClickListener {
            // Trigger house generation if needed
            houseOperations.generateRandomHousesAndUpload()
        }

        binding.imageFilter.setOnClickListener {
            houseOperations.deleteAllHouses()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (areLocationPermissionsGranted()) {
            enableUserLocation()
        } else {
            requestLocationPermissions()
        }

        val defaultLocation = LatLng(43.677308, -79.406927)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.addMarker(MarkerOptions().position(defaultLocation).title("GeorgeBrown"))
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
            if (houseId != null) {
                // Request house details from ViewModel
                mapViewModel.fetchHouseDetails(houseId)
            }
            true
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

    private fun enableUserLocation() {
        // Explicitly check if permissions are granted
        if (isFineLocationPermissionGranted()) {
            try {
                googleMap.isMyLocationEnabled = true // Enable user location on the map
            } catch (e: SecurityException) {
                // Handle the case where permission might be denied or missing
                Toast.makeText(requireContext(), "Error enabling location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // If permission is not granted, request it
            requestLocationPermissions()
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


}
