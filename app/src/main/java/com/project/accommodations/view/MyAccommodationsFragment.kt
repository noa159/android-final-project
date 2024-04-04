package com.project.accommodations.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.accommodations.adapter.MyAccommodationAdapter
import com.project.accommodations.databinding.FragmentMyAccommodationsBinding
import com.project.accommodations.model.Accommodation
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.Query

class MyAccommodationsFragment : Fragment(), MyAccommodationAdapter.AccommodationActionsListener {

    private var _binding: FragmentMyAccommodationsBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }
    private lateinit var accommodationsArrayList: ArrayList<Accommodation>
    private lateinit var myAccommodationsAdapter: MyAccommodationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMyAccommodationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accommodationsArrayList = ArrayList()

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        getData()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        myAccommodationsAdapter = MyAccommodationAdapter(accommodationsArrayList).also {
            it.setOnDeleteCompleteListener(this)
        }
        binding.recyclerView.adapter = myAccommodationsAdapter
    }

    override fun onDelete(id: String, position: Int) {
        db.collection("Accommodations").document(id).delete().addOnSuccessListener {
            accommodationsArrayList.removeAt(position)
            myAccommodationsAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
        }
    }

    override fun onEdit(id: String, position: Int) {
        val action = MyAccommodationsFragmentDirections.actionMyAccommodationsFragmentToEditFragment(id)
        findNavController().navigate(action)
    }

    private fun getData() {
        auth.currentUser?.email?.let { userEmail ->
            db.collection("Accommodations").whereEqualTo("userEmail", userEmail)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Toast.makeText(context, error.localizedMessage, Toast.LENGTH_LONG).show()
                    } else {
                        accommodationsArrayList.clear()
                        value?.documents?.forEach { document ->
                            val id = document.id
                            val comment = document.getString("comment") ?: ""
                            val userEmail = document.getString("userEmail") ?: ""
                            val name = document.getString("name") ?: ""
                            val phone = document.getString("phone") ?: ""
                            val downloadUrl = document.getString("downloadUrl") ?: ""
                            val latitude = document.getDouble("latitude") ?: 0.0
                            val longitude = document.getDouble("longitude") ?: 0.0
                            val accommodation = Accommodation(id, name, userEmail, comment, phone, longitude, latitude, downloadUrl)

                            accommodation?.let { accommodationsArrayList.add(it) }
                        }
                        myAccommodationsAdapter.notifyDataSetChanged()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}