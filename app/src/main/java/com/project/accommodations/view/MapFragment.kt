package com.project.accommodations.view
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.project.accommodations.R
import com.project.accommodations.databinding.FragmentMapBinding
import com.project.accommodations.model.Accommodation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.accommodations.database.AppDatabase
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment(), MapListener {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences(getString(R.string.app_name), AppCompatActivity.MODE_PRIVATE))
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.setMultiTouchControls(true)

        auth = Firebase.auth


        getData()

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mMap)
        controller = mMap.controller

        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true

        mMyLocationOverlay.runOnFirstFix {
            requireActivity().runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation)
                controller.animateTo(mMyLocationOverlay.myLocation)
                controller.setZoom(13.0)
            }
        }

        controller.setZoom(8.0)
        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)

        binding.addAccommodation.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_addAccommodationFragment)
        }

        binding.myAccommodations.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_myAccommodationsFragment)
        }

        binding.btnLogut.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_mapFragment_to_loginFragment)
        }

    }

    private fun getData() {
        val db = AppDatabase.getInstance(requireContext())
        val accommodationDao = db.accommodationDao()

        accommodationDao.getAllOrderedByDateDesc().observe(viewLifecycleOwner) { accommodations ->
            accommodations.forEach { accommodation ->
                addMarker(accommodation)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return false
    }


    fun openDetailComponent(id: String) {
        val action = MapFragmentDirections.actionMapFragmentToAccommodationDetailsFragment(id)
        findNavController().navigate(action)
    }

    private fun addMarker(accommodation : Accommodation) {
        val geoPoint = GeoPoint(accommodation.latitude , accommodation.longitude)

        try {
            val marker = Marker(mMap)
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.marker_default)

            marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { _, _ ->
                openDetailComponent(accommodation.id)
                true
            })

            mMap.overlays.add(marker)
        } catch (e: Exception) {
            Log.e("MapMarkerError", "Error adding marker to map", e)
        }
    }

}

