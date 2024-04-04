package com.project.accommodations.view

import android.content.Intent
import android.graphics.Rect
import android.location.GpsStatus
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.project.accommodations.databinding.ActivityMapBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.accommodations.R
import com.project.accommodations.model.Accommodation
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

class MapActivity : AppCompatActivity(), MapListener, GpsStatus.Listener {
    private lateinit var mMap: MapView
    private lateinit var controller: IMapController;
    private lateinit var db: FirebaseFirestore
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay;
    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())

        auth = Firebase.auth
        db = Firebase.firestore

        getData()

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        controller = mMap.controller


        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true

        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation)
                controller.animateTo(mMyLocationOverlay.myLocation)
                controller.setZoom(13.0)
            }
        }

        controller.setZoom(8.0)
        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)

    }

    private fun getData() {
        db.collection("Accommodations").orderBy("date",
            Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
            } else {
                if (value != null) {
                    if (!value.isEmpty) {
                        val documents = value.documents
                        for (document in documents) {
                            val id = document.id;
                            val comment = document.get("comment") as String
                            val userEmail = document.get("userEmail") as String
                            val name = document.get("name") as String
                            val phone = document.get("phone") as String
                            val downloadUrl = document.get("downloadUrl") as String
                            val latitude = document.get("latitude") as Double
                            val longitude = document.get("longitude") as Double
                            val accommodation = Accommodation(id,name , userEmail, comment,phone ,longitude , latitude, downloadUrl);
                            addMarker(accommodation)
                        }
                    }
                }
            }
        }
    }
    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return false;
    }

    override fun onGpsStatusChanged(event: Int) {
        TODO("Not yet implemented")
    }

    fun upload(view: View){
        val intent = Intent(this, UploadActivity::class.java)
        startActivity(intent)
    }
    fun myAccommodations(view: View){
        val intent = Intent(this, MyAccommodationsActivity::class.java)
        startActivity(intent)
    }


    fun signOut(view: View){
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun openDetailComponent(id: String) {
        val intent = Intent(this, AccommodationViewActivity::class.java).apply {
            putExtra("id", id)
        }
        startActivity(intent)
    }

    private fun addMarker(accommodation : Accommodation) {
        val geoPoint = GeoPoint(accommodation.latitude , accommodation.longitude)

        val marker = Marker(mMap)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon =  ContextCompat.getDrawable(this, R.drawable.marker_default)

        marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { marker, mapView ->
            openDetailComponent(accommodation.id)
            true
        })

        mMap.overlays.add(marker)
    }

}

