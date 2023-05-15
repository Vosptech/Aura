package com.example.aura

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    // creating variables on below line.
    lateinit var responseTV: TextView
    lateinit var questionTV: TextView
   lateinit  var btn:Button
   lateinit var currentNumber:String
    private var context: String =""
    lateinit var queryEdt: TextInputEditText
    val tag = "OngoingProcesses"
    val db = Firebase.firestore
   val sidLocation= db.collection("UserId").document("SID")
    val pInfoLocation=db.collection("UserId").document("PInfo")

    var url = "https://api.openai.com/v1/completions"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // initializing variables on below line.
        responseTV = findViewById(R.id.idTVResponse)
        questionTV = findViewById(R.id.idTVQuestion)
        queryEdt = findViewById(R.id.idEdtQuery)
        btn= findViewById(R.id.button)
        getNum()
        btn.visibility=View.INVISIBLE
        btn.setOnClickListener {
            val optionQury = "Continue the above response."
            promptcreator(optionQury)
        }
        // adding editor action listener for edit text on below line.
        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // setting response tv on below line.
                responseTV.text = "Please wait.."
                Log.e(tag, "Send Button clicked")
                // validating text
                if (queryEdt.text.toString().length > 0) {
                    // calling get response to get the response.
                    promptcreator(queryEdt.text.toString())
                } else {
                    Toast.makeText(this, "Please enter your query..", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun promptcreator (query:String){
        Log.e(tag, "Entered prompt creator")
        sidLocation.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("db", "DocumentSnapshot data: ${document.data}")
                    var p1 = document.get("1").toString()
                    var p2 = document.get("2").toString()
                    var p3 = document.get("3").toString()
                    var a1 = document.get("4").toString()
                    var a2 = document.get("5").toString()
                    var a3 = document.get("6").toString()
                    if (p1=="null"){ p1="" }
                    if (p2=="null"){ p2="" }
                    if (p3=="null"){ p3="" }
                    if (a1=="null"){ a1="" }
                    if (a2=="null"){ a2="" }
                    if (a3=="null"){ a3="" }
                    Log.e(tag, "p1:$p1 ,p2:$p2 ,p3:$p3 , a1:$a1 ,a2:$a2 ,a3:$a3 ")
                    if (currentNumber=="1"){
                        chatCompletion(query,p1,p2,p3,a1,a2,a3)
                    }else if (currentNumber=="2"){
                        chatCompletion(query,p3,p2,p1,a3,a2,a1)
                    }else if (currentNumber=="3"){
                        chatCompletion(query,p3,p1,p2,a3,a1,a2)
                    }

//                context = "$p1\n$p2\n$p3\n$p4\n$p5"
//                    context = "$p1\n$p2"
//                    if(p1=="null"&&p2=="null"){
//
//                    }else {
//
//                        getResponse(query,context)
//                    }
                } else {
                    Log.d("db", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("db", "get failed with ", exception)
            }
    }
    private fun getNum(){
        Log.e(tag, "Entered get Num")
        pInfoLocation.get()
            .addOnSuccessListener{document ->
                if(document!=null){
                    val num =document.get("Num")
                    if (num!=null){
                       currentNumber=num.toString()
                        if (currentNumber=="null"){
                            currentNumber="1"
                        }
                    }
                    Log.e(tag, "NUm is:$currentNumber")
                }
            }
    }
    private fun promptUpload(prompt:String,ans:String){
        Log.e(tag, "Entered prompt upload")
        val num = currentNumber
        val uData = hashMapOf(num to prompt)
        sidLocation.set(uData, SetOptions.merge())
            .addOnSuccessListener {
                val num1 = num.toInt()
                val num2 = num1+3
                val aData = hashMapOf(num2.toString() to ans)
                sidLocation.set(aData, SetOptions.merge()).addOnSuccessListener {
                    increment(num.toInt())
                    Log.e(tag, "upload successful")
                }
                    .addOnFailureListener{

                }

            }
            .addOnFailureListener{

            }
    }
    private fun increment(num:Int){
        Log.e(tag, "Entered Number Increment")
        if (num<3){
            val iNum = num+1
            val pNum = hashMapOf("Num" to iNum)
            pInfoLocation.set(pNum)
            currentNumber = iNum.toString()
        }else{
            val pNum = hashMapOf("Num" to 1)
            pInfoLocation.set(pNum)
            currentNumber="1"
        }
    }
    private fun chatCompletion(currentP:String,p1:String,p2:String,p3:String,a1:String,a2:String,a3:String){
        Log.e(tag, "Entered chat completion, Current prompt:$currentP")
        questionTV.text = currentP
        queryEdt.setText("")
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            val MEDIA_TYPE = "application/json".toMediaType()

            val requestBody = """
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [ {"role": "system", "content": "You are a female emotional,mental supporter and motivator assistant which answers short but in impactful way , Your Name is Aura."},
        {"role": "user", "content": "$p1"},
        {"role": "assistant", "content": "$a1"},
         {"role": "user", "content": "$p2"},
        {"role": "assistant", "content": "$a2"},
         {"role": "user", "content": "$p3"},
        {"role": "assistant", "content": "$a3"},
        {"role": "user", "content": "$currentP"}],
        "max_tokens": 50
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
                    val jsonObject = JSONObject(responseString)
                    val usageObject = jsonObject.getJSONObject("usage")
                    val completionTokens = usageObject.getInt("completion_tokens")
                    val tokens = completionTokens.toInt()
                     Toast.makeText(this@MainActivity,"Tokens:$tokens",Toast.LENGTH_LONG).show()
                    responseTV.text=content
                    tokenCal(tokens)
                    Log.e(tag, "Got response:$content")
                    promptUpload(currentP,content)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun tokenCal(token:Int){
        if (token==50){
            btn.visibility=View.VISIBLE
        }
    }
}