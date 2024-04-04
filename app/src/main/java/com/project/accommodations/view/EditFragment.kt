package com.project.accommodations.view

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import com.project.accommodations.databinding.FragmentEditBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.project.accommodations.model.Accommodation
import com.squareup.picasso.Picasso
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.UUID


class EditFragment : Fragment() {

    private val args: EditFragmentArgs by navArgs()
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var tapOverlay: MapTapOverlay
    private var selectedPicture: Uri? = null
    private lateinit var selectedPosition: GeoPoint
    private lateinit var basicImageUrl: String
    private lateinit var id: String

    // Firebase instances
    private val auth by lazy { Firebase.auth }
    private val firestore by lazy { Firebase.firestore }
    private val storage by lazy { Firebase.storage }

    // ActivityResultLauncher
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val intentFromResult = result.data
            if (intentFromResult?.data != null) {
                selectedPicture = intentFromResult.data
                selectedPicture?.let {
                    binding.imageView.setImageURI(it)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize map and other setup here
        setupMap()
        id = args.id
        fetchAccommodation()

        binding.imageView.setOnClickListener { selectImage() }
        binding.uploadButton.setOnClickListener { findNavController().popBackStack() }
        binding.cancelButton.setOnClickListener { update() }
    }

    private fun setupMap() {
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())
        controller = mMap.controller
        controller.setZoom(8.0)

        val israel = GeoPoint(31.4117257, 35.0818155)
        controller.setCenter(israel)

        tapOverlay = MapTapOverlay() { geoPoint ->
            selectedPosition = geoPoint;
            updateMarkerAtLocation(geoPoint)
        }

        mMap.overlays.add(tapOverlay)
    }

    class MapTapOverlay(private val onMapTap: (GeoPoint) -> Unit) : Overlay() {
        override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
            val projection = mapView.projection
            val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
            onMapTap(geoPoint)
            return true
        }
        override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {

        }
    }



    private fun updateMarkerAtLocation(geoPoint: GeoPoint) {
        mMap.overlays.clear()
        mMap.overlays.add(tapOverlay)

        val marker = Marker(mMap).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Selected Location"
        }
        mMap.overlays.add(marker)
        mMap.invalidate()
    }

    fun update() {
        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"
        val reference = storage.reference
        val imageReference = reference.child("Accommodations").child(imageName)

        if (selectedPicture != null) {
            imageReference.putFile(selectedPicture!!).addOnSuccessListener {

                imageReference.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString()

                    val postMap = hashMapOf<String, Any>()
                    postMap.put("downloadUrl", downloadUrl)
                    postMap.put("userEmail", auth.currentUser!!.email!!)
                    postMap.put("name", binding.nameText.text.toString())
                    postMap.put("phone", binding.phone.text.toString())
                    postMap.put("comment", binding.commentText.text.toString())
                    postMap.put("longitude", selectedPosition.longitude)
                    postMap.put("latitude", selectedPosition.latitude)
                    postMap.put("date", Timestamp.now())


                    firestore.collection("Accommodations").document(id).update(postMap).addOnSuccessListener{
                        findNavController().popBackStack()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Permission needed!", Toast.LENGTH_LONG)
                            .show()
                    }



                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Permission needed!", Toast.LENGTH_LONG)
                    .show()
            }
        }
        else {

            val postMap = hashMapOf<String, Any>()
            postMap.put("downloadUrl", basicImageUrl)
            postMap.put("userEmail", auth.currentUser!!.email!!)
            postMap.put("name", binding.nameText.text.toString())
            postMap.put("phone", binding.phone.text.toString())
            postMap.put("comment", binding.commentText.text.toString())
            postMap.put("longitude", selectedPosition.longitude)
            postMap.put("latitude", selectedPosition.latitude)
            postMap.put("date", Timestamp.now())


            firestore.collection("Accommodations").document(id).update(postMap).addOnSuccessListener{
                findNavController().popBackStack()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Permission needed!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
    private fun fetchAccommodation() {
        firestore.collection("Accommodations").document(id).get().addOnSuccessListener { document ->
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
                val accommodation = Accommodation(id, name, userEmail, comment, phone, longitude, latitude, downloadUrl)

                binding.commentText.setText(accommodation.comment);
                binding.nameText.setText(accommodation.name);
                binding.phone.setText(accommodation.phone);
                Picasso.get().load(accommodation.downloadUrl)
                    .into(binding.imageView)

                basicImageUrl = accommodation.downloadUrl;


                selectedPosition = GeoPoint(accommodation.latitude,accommodation.longitude)

                val marker = Marker(mMap).apply {
                    position = selectedPosition
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Selected Location"
                }

                mMap.overlays.add(marker)
                controller.setCenter(selectedPosition)
                mMap.invalidate()
                controller.setZoom(14.0)

            } else {
                Toast.makeText(requireContext(), "No document found", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }


    fun selectImage() {
        val intentToGallery =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intentToGallery)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





}