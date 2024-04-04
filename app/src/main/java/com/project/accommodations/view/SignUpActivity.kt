package com.project.accommodations.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.project.accommodations.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, AccommodationViewActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    fun signUpClicked(view: View) {
        email = binding.email.text.toString()
        password = binding.password.text.toString()

        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Enter email and password!", Toast.LENGTH_LONG).show()

        } else {
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                //success
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this@SignUpActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
            }

        }

    }
    fun signInClicked(view: View) {
        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

