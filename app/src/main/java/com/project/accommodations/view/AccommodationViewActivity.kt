package com.project.accommodations.view

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.project.accommodations.databinding.ActivityAccommodationViewBinding
import com.project.accommodations.model.Accommodation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class AccommodationViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccommodationViewBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var accommodation: Accommodation
    private lateinit var id: String;

    private lateinit var mMap: MapView
    private lateinit var controller: IMapController;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccommodationViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())
        controller = mMap.controller
        controller.setZoom(8.0)


        auth = Firebase.auth
        db = Firebase.firestore

        id = intent.getStringExtra("id") as String;
        fetchAccommodation();

    }

    private fun fetchAccommodation() {
        db.collection("Accommodations").document(id).get().addOnSuccessListener { document ->
            if (document != null) {
                // Extract fields from the document
                val id = document.id
                val comment = document.getString("comment") ?: ""
                val userEmail = document.getString("userEmail") ?: ""
                val name = document.getString("name") ?: ""
                val phone = document.getString("phone") ?: ""
                val downloadUrl = document.getString("downloadUrl") ?: ""
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                accommodation = Accommodation(id, name, userEmail, comment, phone, longitude, latitude, downloadUrl)

                binding.tvComment.text = accommodation.comment
                binding.tvName.text = accommodation.name
                binding.tvPhone.text = accommodation.phone
                Picasso.get().load(accommodation.downloadUrl)
                    .into(binding.ivPostImage)

                val accommodationLocation = GeoPoint(accommodation.latitude,accommodation.longitude)

                val marker = Marker(mMap).apply {
                    position = accommodationLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = accommodation.name
                }

                mMap.overlays.add(marker)
                controller.setCenter(accommodationLocation)
                mMap.invalidate()
                controller.setZoom(14.0)

            } else {
                Toast.makeText(this@AccommodationViewActivity, "No document found", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this@AccommodationViewActivity, exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    fun contact(view : View){
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${accommodation.phone}")
        }
        startActivity(intent)
    }

    fun cancel(view : View){
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        finish()
    }


}