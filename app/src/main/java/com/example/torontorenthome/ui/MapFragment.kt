package com.example.torontorenthome.ui


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.torontorenthome.R
import com.example.torontorenthome.databinding.FragmentMapBinding
import com.example.torontorenthome.models.HouseInfo
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
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: com.google.android.gms.maps.MapView
    private lateinit var googleMap: GoogleMap
    private val houseOperations = HouseOperations()  // Create

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


      //  binding.svSearch.setBackgroundColor(Color.WHITE) // Set the input area background to white

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.tvAppName.setOnClickListener {
            houseOperations.generateRandomHousesAndUpload()
        }
        binding.imageFilter.setOnClickListener{
            houseOperations.deleteAllHouses()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Request location permissions
        if (checkLocationPermission()) {
            enableUserLocation()
        } else {
            requestLocationPermission()
        }

        // Set default map position
        val defaultLocation1 = LatLng(43.677308, -79.406927) // GBCollege
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation1, 12f))

        // Enable zoom controls
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Add a default marker
        googleMap.addMarker(
            MarkerOptions()
                .position(defaultLocation1)
                .title("GeorgeBrown")
        )

        addMarkersToMap()


    }

    private fun checkLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocation == PackageManager.PERMISSION_GRANTED &&
                coarseLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
                // Permission denied: Show a message to the user
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
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

    fun addMarkersToMap() {
        val housesCollection = db.collection("houses")

        housesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {

                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    val description = document.getString("description") ?: "Unknown"
                    val type = document.getString("type") ?: "Unknown"
                    val createTime = document.getString("createTime") ?: "Unknown"
                    val bedrooms = (document.getLong("bedrooms") ?: 0).toInt()
                    val bathrooms = (document.getLong("bathrooms") ?: 0).toInt()
                    val area = (document.getLong("area") ?: 0).toInt()
                    val price = document.getDouble("price") ?: 0.0

                    val houseImage = R.drawable.house01 // Replace with your logic

                    if (latitude != null && longitude != null) {
                        // Create LatLng object
                        val location = LatLng(latitude, longitude)

                        // Add marker to the map
                        val marker=googleMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(description)
                        )
                        // Attach data to the marker
                        marker?.tag = HouseInfo(houseImage, description,type,createTime, bedrooms, price, bathrooms,area,)
                    }
                }
                // Set a marker click listener
                googleMap.setOnMarkerClickListener { marker ->
                    val houseInfo = marker.tag as? HouseInfo
                    houseInfo?.let {
                        val bottomSheet = HouseInfoBottomSheet.newInstance(
                            it.image, it.description,it.type,it.createTime, it.bedrooms, it.price,it.bathrooms,it.area
                        )
                        bottomSheet.show(parentFragmentManager, "HouseInfoBottomSheet")
                    }
                    true
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching houses: ${e.message}")
            }
    }
}