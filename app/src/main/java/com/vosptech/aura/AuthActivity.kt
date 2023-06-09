package com.vosptech.aura

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private val TAG = "Auth Process"
    private lateinit var signInWithGoogleBtn:Button
    private lateinit var auth: FirebaseAuth
    private var showOneTapUI = true
    private lateinit var emaiEditText:EditText
    private lateinit var verifyEmailBTn:Button
    private lateinit var signUpRequest: BeginSignInRequest
    private var signInRequest: BeginSignInRequest? = null
    private var oneTapClient: SignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        // Initialize Firebase Auth
        auth = Firebase.auth
        emaiEditText=findViewById(R.id.editTextTextEmailAddress)
        verifyEmailBTn=findViewById(R.id.emailVerify)
        verifyEmailBTn.setOnClickListener {
            val email = emaiEditText.text.toString()
            sendEmailVerificationLink(email)
        }
        signInWithGoogleBtn = findViewById(R.id.signInWithGoogle)
        signInWithGoogleBtn.setOnClickListener {
            BuildRequest()
        }

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
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
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
                Log.d(TAG,"failed to get saved credentials")
                Log.d(TAG, e.localizedMessage.toString())
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
                        Log.d(TAG, f.localizedMessage.toString())
                    }
            }
    }
    private fun updateUI(currentUser:FirebaseUser?){
        if (currentUser != null){
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this,"Failed to Login",Toast.LENGTH_LONG).show()
        }
    }

    private fun sendEmailVerificationLink(email: String) {
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setHandleCodeInApp(true)
            .setUrl("https://vosptech.com/")
            .setAndroidPackageName(
                packageName,
                /* installIfNotAvailable= */ false,
                /* minimumVersion= */ null
            )
            .build()

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email verification link sent.")
                    // Show a success message or proceed with your app logic
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Failed to send email verification link.", exception)
                    if (exception is FirebaseAuthInvalidUserException) {
                        // The email address is not registered with Firebase
                    } else {
                        // Other error handling
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()

        // Check if the activity was opened from an email link
        val intentData = intent.data
        if (intentData != null && auth.isSignInWithEmailLink(intentData.toString())) {
            val email = intentData.getQueryParameter("email")
            val oobCode = intentData.getQueryParameter("oobCode")

            // Complete email verification
            if (email != null && oobCode != null) {
                auth.signInWithEmailLink(email, oobCode)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email verification complete.")
                            val user = task.result?.user
                            val currentUser = auth.currentUser
                            updateUI(currentUser)
                            // Continue with your app logic
                        } else {
                            val exception = task.exception
                            Log.e(TAG, "Failed to complete email verification.", exception)
                            if (exception is FirebaseAuthActionCodeException) {
                                // The email link has expired or has already been used
                            } else if (exception is FirebaseAuthInvalidCredentialsException) {
                                // The email link is malformed or not valid
                            } else {
                                // Other error handling
                            }
                        }
                    }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
