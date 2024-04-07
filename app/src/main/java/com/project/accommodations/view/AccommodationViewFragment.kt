package com.project.accommodations.view

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.accommodations.database.AppDatabase
import com.project.accommodations.databinding.FragmentAccommodationViewBinding
import com.project.accommodations.model.Accommodation
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class AccommodationViewFragment : Fragment() {

    private val args: AccommodationViewFragmentArgs by navArgs()
    private var _binding: FragmentAccommodationViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var accommodation: Accommodation
    private lateinit var id: String

    private lateinit var mMap: MapView
    private lateinit var controller: IMapController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccommodationViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMap = binding.osmmap.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
        controller = mMap.controller.apply {
            setZoom(8.0)
        }


        id = args.id;

        fetchAccommodation()

        binding.btnContact.setOnClickListener { contact() }
        binding.btnCancel.setOnClickListener { cancel() }
    }

    private fun fetchAccommodation() {
        val accommodationDao = AppDatabase.getInstance(requireContext()).accommodationDao()

        accommodationDao.getById(id).observe(viewLifecycleOwner) { accommodation ->
            if (accommodation != null) {
                binding.tvComment.text = accommodation.comment
                binding.tvName.text = accommodation.name
                binding.tvPhone.text = accommodation.phone
                Picasso.get().load(accommodation.downloadUrl).into(binding.ivPostImage)

                val accommodationLocation = GeoPoint(accommodation.latitude, accommodation.longitude)
                val marker = Marker(mMap).apply {
                    position = accommodationLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = accommodation.name
                }

                mMap.overlays.clear()
                mMap.overlays.add(marker)
                controller.setCenter(accommodationLocation)
                mMap.invalidate()
                controller.setZoom(14.0)
            } else {
                Toast.makeText(requireContext(), "No accommodation found", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun contact() {
        startActivity(Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${accommodation.phone}")
        })
    }

    private fun cancel() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}