package com.project.accommodations.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.project.accommodations.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun signInClicked(view: View) {
        email = binding.email.text.toString()
        password = binding.password.text.toString()
        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Email and password!", Toast.LENGTH_LONG).show()
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                val intent = Intent(this@MainActivity, MapActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }

    }

    fun signUpClicked(view: View) {
        val intent = Intent(this@MainActivity, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }
}