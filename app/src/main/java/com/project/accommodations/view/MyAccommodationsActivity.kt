package com.project.accommodations.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.accommodations.adapter.MyAccommodationAdapter
import com.project.accommodations.databinding.ActivityMyAccommodationsBinding
import com.project.accommodations.model.Accommodation

class MyAccommodationsActivity : AppCompatActivity(), MyAccommodationAdapter.AccommodationActionsListener {

    private lateinit var binding: ActivityMyAccommodationsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var accommodationsArrayList: ArrayList<Accommodation>
    private lateinit var myAccommodationsAdapter: MyAccommodationAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccommodationsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = Firebase.firestore

        accommodationsArrayList= ArrayList<Accommodation> ()

        getData()

        binding.recyclerView.layoutManager= LinearLayoutManager(this)
        myAccommodationsAdapter= MyAccommodationAdapter(accommodationsArrayList).also {
            it.setOnDeleteCompleteListener(this)
        }
        binding.recyclerView.adapter=myAccommodationsAdapter

    }

    override fun onDelete(id: String, position: Int) {
        db.collection("Accommodations").document(id).delete().addOnSuccessListener {
            accommodationsArrayList.removeAt(position);
            myAccommodationsAdapter.notifyDataSetChanged()
        }
            .addOnFailureListener {
            Toast.makeText(this@MyAccommodationsActivity, "Error", Toast.LENGTH_LONG).show()
        }
    }

    override fun onEdit(id: String, position: Int) {
        val intent = Intent(this, EditActivity::class.java).apply {
            putExtra("id", id)
        }
        startActivity(intent)
    }

    private fun getData() {
        val userEmail = auth.currentUser!!.email!!
        db.collection("Accommodations").whereEqualTo("userEmail", userEmail).orderBy("date",
            Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this@MyAccommodationsActivity, error.localizedMessage, Toast.LENGTH_LONG).show()
            } else {
                if (value != null) {
                    if (!value.isEmpty) {
                        val documents = value.documents
                        accommodationsArrayList.clear()
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
                            accommodationsArrayList.add(accommodation)

                        }
                        myAccommodationsAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun returnToMap(view: View){
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        finish()
    }


}