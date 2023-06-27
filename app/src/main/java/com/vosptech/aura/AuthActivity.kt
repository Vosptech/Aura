package com.vosptech.aura

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Random
import java.util.Timer
import java.util.TimerTask

class AuthActivity : AppCompatActivity() {
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private val TAG = "AuthProcess"
    private var isNewUser: Boolean? = null
    private lateinit var signInWithGoogleBtn:Button
    private lateinit var auth: FirebaseAuth
    private var showOneTapUI = true
    private lateinit var progressBar: ProgressBar
    private lateinit var questionTextView: TextView
    private lateinit var answereTextView: TextView
    private val responsesList= mutableListOf<Pair<String,String>>()
    private lateinit var signUpRequest: BeginSignInRequest
    private var signInRequest: BeginSignInRequest? = null
    private var oneTapClient: SignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        actionBar?.hide()
        supportActionBar?.hide()

        // Initialize Firebase Auth
        auth = Firebase.auth
        progressBar=findViewById(R.id.progressBarAuth)
        progressBar.visibility=View.GONE
        questionTextView=findViewById(R.id.messageTextView2)
        answereTextView=findViewById(R.id.messageTextView3)
       responsesList.add(Pair("I'm feeling really stressed and overwhelmed right now. What can I do to relax and calm my mind?",
           "Imagine yourself on a beach with the waves washing over your feet. Breathe in the salty air and feel the warmth of the sun on your skin. Visualize your worries washing away with the tide. Focus on the sensations and let yourself fully relax."))
        responsesList.add(Pair("Sometimes I feel really down and hopeless. How can I find motivation and regain a positive outlook?",
            "You are the only person in the world with your unique combination of talents, experiences, and perspectives. Embrace your individuality and use it to your advantage to create a life that is fulfilling and meaningful to you."))
       responsesList.add(Pair("I'm experiencing burnout and exhaustion. How can I recharge and find a healthy work-life balance?",
           "Prioritizing your well-being is not a selfish act, but rather a necessary one for a more meaningful and fulfilling life. Take time to recharge, find a healthy work-life balance, and make your overall health and happiness a priority." ))
        responsesList.add(Pair("I am feeling sad can you make me happy.","Sure,here's a short and funny Joke:\n Why don't scientists trust atoms?\nBecause they make up everything.\n\nI hope you liked it!"))
      loadSampleMessages()

        signInWithGoogleBtn = findViewById(R.id.signInWithGoogle)
        signInWithGoogleBtn.setOnClickListener {
            progressBar.visibility=View.VISIBLE
            BuildRequest()
        }

    }
     fun loadSampleMessages() {

                 val timer = Timer()
                 val task = object : TimerTask() {
                     override fun run() {
                         // Execute your program logic here
                         println("Executing program")
                         val random = Random()
                         val randomNumber = random.nextInt(4)
                         val question=responsesList[randomNumber].first
                         val answer =responsesList[randomNumber].second
                         runOnUiThread {
                             questionTextView.text=question
                             answereTextView.text=answer
                         }

                     }
                 }
                 timer.scheduleAtFixedRate(task, 0, 5000) // Run every 1 second (1000 milliseconds)


         }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient?.getSignInCredentialFromIntent(data)
                    val idToken = credential?.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        isNewUser = task.result?.additionalUserInfo?.isNewUser
                                        val user = auth.currentUser
                                        updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        updateUI(null)
                                    }
                                }
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            progressBar.visibility=View.GONE
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                            Toast.makeText(this,"Network Error,Please Check your Internet Connection",Toast.LENGTH_LONG).show()
                            progressBar.visibility=View.GONE
                        }
                        else -> {
                            progressBar.visibility=View.GONE
                            Log.d(TAG, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }
                    }
                }
            }
        }

    }




    private fun BuildRequest(){
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()
        oneTapClient!!.beginSignIn(signInRequest!!)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d(TAG,"1failed to get saved credentials")
                Log.d(TAG, e.localizedMessage?.toString() ?: "")
                oneTapClient = Identity.getSignInClient(this)
                signUpRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            // Your server's client ID, not your Android client ID.
                            .setServerClientId(getString(R.string.your_web_client_id))
                            // Show all accounts on the device.
                            .setFilterByAuthorizedAccounts(false)
                            .build())
                    .build()
                oneTapClient!!.beginSignIn(signUpRequest)
                    .addOnSuccessListener(this) { result ->
                        try {
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender, REQ_ONE_TAP,
                                null, 0, 0, 0)
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                        }
                    }
                    .addOnFailureListener(this) { f ->
                        // No Google Accounts found. Just continue presenting the signed-out UI.
                        Log.d(TAG,"2")
                        Log.d(TAG, f.localizedMessage.toString())
                    }
            }

    }
    private fun updateUI(currentUser:FirebaseUser?){
        progressBar.visibility=View.GONE
        if (currentUser != null){
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
            if (isNewUser==true){
                //New Users Found48
            }
        }else{
            Toast.makeText(this,"Failed to Login",Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
