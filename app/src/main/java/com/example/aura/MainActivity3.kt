package com.example.aura

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.Manifest
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Locale

class MainActivity3 : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val SPEECH_REQUEST_CODE = 1

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechTextView: TextView
    private lateinit var convertButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        speechTextView = findViewById(R.id.speechTextView)
        convertButton = findViewById(R.id.convertButton)

        // Check for speech recognition permission
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Set click listener for the convert button
        convertButton.setOnClickListener {
            startSpeechRecognition()
        }
    }

    private fun startSpeechRecognition() {
        // Create an intent for speech recognition
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        // Start the speech recognition activity
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)

            // Display the recognized speech
            speechTextView.text = spokenText

            // Call the OpenAI Whisper API to convert speech to text
            convertSpeechToText(spokenText)
        }
    }

    private fun convertSpeechToText(speech: String?) {
        val apiKey = "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"
        val url = "https://api.openai.com/v1/engines/davinci-codex/completions"
        val prompt = "Convert the following speech to text: \"$speech\""

        val requestBody = JSONObject().apply {
            put("prompt", prompt)
            put("max_tokens", 64)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                val choices = response.getJSONArray("choices")
                val text = choices.getJSONObject(0).getString("text")

                // Display the converted text
                speechTextView.append("\n\nConverted Text:\n$text")
            },
            { error ->
                Log.e(TAG, "Error: ${error.message}")
                Toast.makeText(this, "Error occurred. Please try again.", Toast.LENGTH_SHORT).show()
            }
        )

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Release the SpeechRecognizer resources
        speechRecognizer.destroy()
    }
}

