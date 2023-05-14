package com.example.aura

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.System
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity2 : AppCompatActivity() {
    lateinit var tv :TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        tv=findViewById(R.id.textView1)
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            val MEDIA_TYPE = "application/json".toMediaType()

            val requestBody = """
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [{"role": "user", "content": "Hello!"}]
                }
            """.trimIndent()

            val OPENAI_API_KEY = "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"
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(requestBody.toRequestBody(MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $OPENAI_API_KEY")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseString = response.body?.string()

                runOnUiThread {
                    val responseObj = JSONObject(responseString)
                    val choicesArray = responseObj.getJSONArray("choices")
                    val messageObj = choicesArray.getJSONObject(0).getJSONObject("message")
                    val content = messageObj.getString("content")

                    tv.text = content
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
// Do something with the responseString