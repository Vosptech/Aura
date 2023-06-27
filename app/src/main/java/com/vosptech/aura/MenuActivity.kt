package com.vosptech.aura

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var profilePhoto:ImageView
    private lateinit var profileName:TextView
    private lateinit var profileAge:TextView
    private lateinit var profileGender:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        profileAge=findViewById(R.id.ProfileAge)
        profilePhoto=findViewById(R.id.profilePhoto)
        profileName=findViewById(R.id.profileName)
        profileGender=findViewById(R.id.profileGender)
        auth = Firebase.auth
        val user =auth.currentUser
        user?.let {
            val name = it.displayName.toString()
            val email = it.email.toString()
            val gender = user.providerData[0].providerId.toString()  // Returns the provider ID, such as "google.com"
            val ageRange = user.metadata?.creationTimestamp.toString()
            val photoUrl = it.photoUrl
            profileName.text=name
            profileAge.text=ageRange
            profileGender.text=gender

            Glide.with(this)
                .load(photoUrl)
                .into(profilePhoto)
        }
    }

    fun logOut(view: View) {
        Firebase.auth.signOut()
        startActivity(Intent(this,AuthActivity::class.java))
        finish()
    }
}