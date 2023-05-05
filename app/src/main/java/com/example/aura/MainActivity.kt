package com.example.aura

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
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
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    // creating variables on below line.
    lateinit var responseTV: TextView
    lateinit var questionTV: TextView
    private var context: String =""
    lateinit var queryEdt: TextInputEditText
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

        // adding editor action listener for edit text on below line.
        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // setting response tv on below line.
                responseTV.text = "Please wait.."
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

    private fun getResponse(query:String,context:String) {

        val promptText1 = "$query. Previous Chat for context:$context"
        val promptText = promptText1.replace("\\s+".toRegex(), " ")
        // setting text on for question on below line.
        questionTV.text = query
        queryEdt.setText("")
        // creating a queue for request queue.
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        // creating a json object on below line.
        val jsonObject: JSONObject? = JSONObject()
        // adding params to json object.
        Toast.makeText(this, promptText,Toast.LENGTH_LONG).show()
            jsonObject?.put("model", "text-davinci-003")
            jsonObject?.put("prompt", promptText)
            jsonObject?.put("temperature", 0)
            jsonObject?.put("max_tokens", 150)
            jsonObject?.put("top_p", 1)
            jsonObject?.put("frequency_penalty", 0.0)
            jsonObject?.put("presence_penalty", 0.0)

        // on below line making json object request.
        val postRequest: JsonObjectRequest =
            // on below line making json object request.
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response ->
                    // on below line getting response message and setting it to text view.
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0).getString("text")
                    responseTV.text = responseMsg


                    val cText = "User:$query.\nBot:${responseMsg.toString()}"
                    getNum(cText)
                },
                // adding on error listener
                Response.ErrorListener { error ->
                    Log.e("TAGAPI", "Error is : " + error.message + "\n" + error)
                    Toast.makeText(this,"Error is ${error.message}",Toast.LENGTH_LONG).show()
                }) {
                override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                    val params: MutableMap<String, String> = HashMap()
                    // adding headers on below line.
                    params["Content-Type"] = "application/json"
                    params["Authorization"] =
                        "Bearer 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"
                    return params;
                }
            }

        // on below line adding retry policy for our request.
        postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // on below line adding our request to queue.
        queue.add(postRequest)

    }
    private fun promptcreator (query:String){

        sidLocation.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("db", "DocumentSnapshot data: ${document.data}")
                    var p1 = document.get("1").toString()
                    var p2 = document.get("2").toString()
                    var p3 = document.get("3").toString()
                    var p4 = document.get("4").toString()
                    var p5 = document.get("5").toString()
                    if (p1=="null"){ p1="" }
                    if (p2=="null"){ p2="" }
                    if (p3=="null"){ p3="" }
                    if (p4=="null"){ p4="" }
                    if (p5=="null"){ p5="" }
                    context = "$p1\n$p2\n$p3\n$p4\n$p5"
                    if(p1=="null"&&p2=="null"&&p3=="null"&&p4=="null"&&p5=="null"){
                        val promptText=query
                    }else {
                        val promptText = "$query (Previous conversation:$context)"
                        getResponse(query,context)
                    }
                } else {
                    Log.d("db", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("db", "get failed with ", exception)
            }
    }
    private fun getNum(promptAndresponse:String){
        pInfoLocation.get()
            .addOnSuccessListener{document ->
                if(document!=null){
                    val num =document.get("Num")
                    if (num!=null){
                        promptUpload(promptAndresponse,num.toString())
                    }else{
                      promptUpload(promptAndresponse,"1")
                    }
                }
            }
    }
    private fun promptUpload(prompt:String,num:String){
        val uData = hashMapOf(num to prompt)
        sidLocation.set(uData, SetOptions.merge())
            .addOnSuccessListener {
                increment(num.toInt())
            }
            .addOnFailureListener{

            }
    }
    private fun increment(num:Int){
        if (num<5){
            val iNum = num+1
            val pNum = hashMapOf("Num" to iNum)
            pInfoLocation.set(pNum)
        }else{
            val pNum = hashMapOf("Num" to 1)
            pInfoLocation.set(pNum)

        }
    }
}