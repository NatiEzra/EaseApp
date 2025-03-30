package com.example.ease.ui.rest_apis


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ease.R
import com.example.ease.data.Place
import com.example.ease.data.PlaceRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheet: LinearLayout

    private lateinit var bottomSheetTitle: MaterialTextView
    private lateinit var bottomSheetAddress: MaterialTextView
    private lateinit var bottomSheetCity: MaterialTextView
    private lateinit var bottomSheetPhone: MaterialTextView
    private lateinit var bottomSheetWorkingHours: MaterialTextView
    private lateinit var bottomSheetDescription: MaterialTextView
    private lateinit var bottomSheetPhoto: ShapeableImageView

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var myLocationFab: FloatingActionButton
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient


    private val places: List<Place> = PlaceRepository.places

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            filterMarkers(query)
        }

        bottomSheet = view.findViewById(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetTitle = view.findViewById(R.id.bottomSheetTitle)
        bottomSheetAddress = view.findViewById(R.id.bottomSheetAddress)
        bottomSheetCity = view.findViewById(R.id.bottomSheetCity)
        bottomSheetPhone = view.findViewById(R.id.bottomSheetPhone)
        bottomSheetWorkingHours = view.findViewById(R.id.bottomSheetWorkingHours)
        bottomSheetDescription = view.findViewById(R.id.bottomSheetDescription)
        bottomSheetPhoto = view.findViewById(R.id.bottomSheetPhoto)


        myLocationFab = view.findViewById(R.id.myLocationFab)
        myLocationFab.setOnClickListener {
            moveCameraToUserLocation()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onMapReady(map: GoogleMap) {
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireContext())
        googleMap = map

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        if (places.isNotEmpty()) {
            val firstPlace = places[0]
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPlace.lat, firstPlace.lng), 10f))
        }

        addMarkers(places)

        googleMap.setOnMarkerClickListener { marker ->
            Log.d("MapFragment", "Marker clicked: ${marker.title}")
            val place = marker.tag as? Place
            if (place != null) {
                showPlaceDetails(place)
                true
            } else {
                false
            }
        }

        googleMap.setOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun addMarkers(placesList: List<Place>) {
        googleMap.clear()
        for (place in placesList) {
            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.pin)

            val width = 80
            val height = 80
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

            val markerOptions = MarkerOptions()
                .position(LatLng(place.lat, place.lng))
                .title(place.name)
                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))

            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = place
        }
    }


    private fun showPlaceDetails(place: Place) {
        bottomSheetTitle.text = place.name
        bottomSheetAddress.text = "Address: ${place.address}"
        bottomSheetCity.text = "City: ${place.city}"
        bottomSheetPhone.text = "Phone: ${place.phone}"
        bottomSheetWorkingHours.text = "Working Hours: ${place.workingHours}"
        bottomSheetDescription.text = "Description: ${place.description}"
        Picasso.get().load(place.photoUrl).into(bottomSheetPhoto)
        bottomSheet.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun filterMarkers(query: String) {
        val filtered = places.filter {
            it.name.contains(query, ignoreCase = true) || it.city.contains(query, ignoreCase = true)
        }
        addMarkers(filtered)
    }

    private fun moveCameraToUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                } else {
                    Toast.makeText(requireContext(), "Location not available yet", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    }