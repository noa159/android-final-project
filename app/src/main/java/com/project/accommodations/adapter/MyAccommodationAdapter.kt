package com.project.accommodations.adapter

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.project.accommodations.databinding.MyAccommodationRowBinding
import com.project.accommodations.model.Accommodation
import com.squareup.picasso.Picasso
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker



class MyAccommodationAdapter(private val accommodationList: ArrayList<Accommodation>) :
    RecyclerView.Adapter<MyAccommodationAdapter.AccommodationHolder>() {
    interface AccommodationActionsListener {
        fun onDelete(id: String, position: Int)
        fun onEdit(id: String, position: Int)
    }

    private var onDeleteCompleteListener: AccommodationActionsListener? = null
    class AccommodationHolder(val binding: MyAccommodationRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccommodationHolder {
        val binding = MyAccommodationRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccommodationHolder(binding)
    }
    fun setOnDeleteCompleteListener(listener: AccommodationActionsListener) {
        onDeleteCompleteListener = listener
    }


    override fun onBindViewHolder(holder: AccommodationHolder, positionInList: Int) {
        holder.binding.tvComment.text = accommodationList.get(positionInList).comment
        holder.binding.tvName.text = accommodationList.get(positionInList).name

        val accommodation = accommodationList.get(positionInList)
        val mMap = holder.binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())
        val controller = mMap.controller
        controller.setZoom(8.0)
        val accommodationLocation = GeoPoint(accommodation.latitude,accommodation.longitude)

        val marker = Marker(mMap).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            position = accommodationLocation
            title = accommodation.name
        }

        mMap.overlays.add(marker)
        controller.setCenter(accommodationLocation)
        mMap.invalidate()
        controller.setZoom(14.0)


        holder.binding.btnRemove.setOnClickListener {
            if (positionInList != RecyclerView.NO_POSITION) {
                val id = accommodationList[positionInList].id
                onDeleteCompleteListener?.onDelete(id, positionInList)
            }
        }

        holder.binding.btnEdit.setOnClickListener {
            val position = positionInList
            if (position != RecyclerView.NO_POSITION) {
                val id = accommodationList[position].id
                onDeleteCompleteListener?.onEdit(id, position)
            }
        }
    }

    fun removeItemAt(index: Int){
        accommodationList.removeAt(index)

    }
    override fun getItemCount(): Int {
        return accommodationList.size
    }
}