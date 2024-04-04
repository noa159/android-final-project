package com.project.accommodations.view

import android.Manifest
import android.app.Activity.RESULT_OK
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.project.accommodations.databinding.FragmentUploadBinding
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.*

class UploadFragment  : Fragment() {
    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var tapOverlay: MapTapOverlay
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedPicture: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedPosition: GeoPoint


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false) // Adjust based on your actual layout file name
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerLauncher()

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

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

        binding.uploadButton.setOnClickListener { upload() }
        binding.imageView.setOnClickListener { selectImage() }
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    class MapTapOverlay(private val onMapTap: (GeoPoint) -> Unit) : Overlay() {
        override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
            // Convert screen position to geo position
            val projection = mapView.projection
            val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
            onMapTap(geoPoint)
            return true
        }
        override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    fun upload() {
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


                    firestore.collection("Accommodations").add(postMap).addOnSuccessListener{
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
    }

    fun selectImage() {
        val intentToGallery =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intentToGallery)
    }

    private fun registerLauncher() {
         activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult?.data != null) {
                        selectedPicture = intentFromResult.data
                        selectedPicture?.let {
                            binding.imageView.setImageURI(it)
                        }
                    }
                }

            }
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //permission granted
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    //permission denied
                    Toast.makeText(requireContext(), "Permission needed!", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}