package com.vosptech.aura

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
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
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    // creating variables on below line.
    private lateinit var continueAboveResponsebtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ChatAdapter
    private lateinit var rootView: View
    private lateinit var recyclerViewBottomSheet: RecyclerView
    private val sessions = mutableListOf<Session>()
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var closeImageViewBtn: ImageView
    private lateinit var sessionNameEditText: TextInputEditText
    private lateinit var sessionNameOutBox: TextInputLayout
    private lateinit var newChat: LinearLayoutCompat
    private lateinit var startChatBtn:Button
    private lateinit var userId:String
    private lateinit var currentNumber: String
    private lateinit var queryEdt: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var menuBtn : ImageView
    private val tag = "OngoingProcesses"
    private val message = mutableListOf<String>()
    private val db = Firebase.firestore
    private lateinit var sidLocation :DocumentReference
    private lateinit var pInfoLocation:DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // initializing variables on below line.
        openSessionBottomSheet()
        queryEdt = findViewById(R.id.idEdtQuery)
        continueAboveResponsebtn = findViewById(R.id.button)
        currentNumber="1"
        auth=Firebase.auth
        val currentUser = auth.currentUser
         userId= currentUser?.uid.toString()
        menuBtn=findViewById(R.id.menuButton)
        menuBtn.setOnClickListener {
            val intent = Intent(this,MenuActivity::class.java)
            startActivity(intent)
        }
        sidLocation = db.collection(userId).document("SessionId").collection("context").document("chatContext")
        pInfoLocation = db.collection(userId).document("SessionId")
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Create and set the adapter for the RecyclerView
        adapter = ChatAdapter(message)
        recyclerView.adapter = adapter
        continueAboveResponsebtn.visibility = View.GONE
        continueAboveResponsebtn.setOnClickListener {
            continueAboveResponsebtn.visibility=View.GONE
            val optionQury = "Continue the above response."
            promptcreator(optionQury)
        }
        sendBtn=findViewById(R.id.sendBtn)
        sendBtn.setOnClickListener {
            if (queryEdt.text.toString().isNotEmpty()) {
                // calling get response to get the response.
                promptcreator(queryEdt.text.toString())
            } else {
            Toast.makeText(this, "Please enter your query..", Toast.LENGTH_SHORT).show()
        }
        }
        // adding editor action listener for edit text on below line.
        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // setting response tv on below line.

                Log.e(tag, "Send Button clicked")
                // validating text
                if (queryEdt.text.toString().isNotEmpty()) {
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

    private fun promptcreator(query: String) {
        continueAboveResponsebtn.visibility=View.GONE
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
                    if (p1 == "null") {
                        p1 = ""
                    }
                    if (p2 == "null") {
                        p2 = ""
                    }
                    if (p3 == "null") {
                        p3 = ""
                    }
                    if (a1 == "null") {
                        a1 = ""
                    }
                    if (a2 == "null") {
                        a2 = ""
                    }
                    if (a3 == "null") {
                        a3 = ""
                    }

                    val ap1 = optimizeStringForJson(p1)
                    val ap2 = optimizeStringForJson(p2)
                    val ap3 = optimizeStringForJson(p3)
                    val aa1 = optimizeStringForJson(a1)
                    val aa2 = optimizeStringForJson(a2)
                    val aa3 = optimizeStringForJson(a3)
                    val queryN = "1$query"
                    updateRview(queryN)
                    closeKeyboard(this)
                    updateRview("2Please wait...")
                    scrollToBottom()

                    Log.e(tag, "p1:$p1 ,p2:$p2 ,p3:$p3 , a1:$a1 ,a2:$a2 ,a3:$a3 ")
                    if (currentNumber == "1") {
                        chatCompletion(query, ap1, ap2, ap3, aa1, aa2, aa3)

                    } else if (currentNumber == "2") {
                        chatCompletion(query, ap2, ap3, ap1, aa3, aa2, aa1)

                    } else if (currentNumber == "3") {
                        chatCompletion(query, ap3, ap1, ap2, aa3, aa1, aa2)

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

    private fun getNum() {
        Log.e(tag, "Entered get Num")
        pInfoLocation.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val num = document.get("Num")
                    if (num != null) {
                        currentNumber = num.toString()

                        if (currentNumber == "null") {
                            currentNumber = "1"
                        }
                        historyDownload()
                    }
                    Log.e(tag, "NUm is:$currentNumber")
                }
            }
    }

    private fun promptUpload(prompt: String, ans: String) {
        Log.e(tag, "Entered prompt upload")
        val num = currentNumber
        val uData = hashMapOf(num to prompt)
        sidLocation.set(uData, SetOptions.merge())
            .addOnSuccessListener {
                val num1 = num.toInt()
                val num2 = num1 + 3
                val aData = hashMapOf(num2.toString() to ans)
                sidLocation.set(aData, SetOptions.merge())
                    .addOnSuccessListener {
                    increment(num.toInt())
                    Log.e(tag, "upload successful")
                }
                    .addOnFailureListener {

                    }

            }
            .addOnFailureListener {

            }
    }

    private fun increment(num: Int) {
        Log.e(tag, "Entered Number Increment")
        if (num < 3) {
            val iNum = num + 1
            val pNum = hashMapOf("Num" to iNum)
            pInfoLocation.set(pNum)
            currentNumber = iNum.toString()
        } else {
            val pNum = hashMapOf("Num" to 1)
            pInfoLocation.set(pNum)
            currentNumber = "1"
        }
    }

    private fun chatCompletion(
        currentP: String,
        p1: String,
        p2: String,
        p3: String,
        a1: String,
        a2: String,
        a3: String
    ) {
        Log.e(tag, "Entered chat completion, Current prompt:$currentP")
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
        "max_tokens": 150
                }
            """.trimIndent()
            Log.d("prompts", requestBody)
            val OPENAI_API_KEY = "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"
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(requestBody.toRequestBody(MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $OPENAI_API_KEY")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseString = response.body!!.string()

                runOnUiThread {

                    val responseObj = JSONObject(responseString)
                    Log.d("Response", responseString)
                    val choicesArray = responseObj.getJSONArray("choices")
                    val messageObj = choicesArray.getJSONObject(0).getJSONObject("message")
                    val content = messageObj.getString("content")
                    val jsonObject = JSONObject(responseString)
                    val usageObject = jsonObject.getJSONObject("usage")
                    val completionTokens = usageObject.getInt("completion_tokens")
                    val promptTokens = usageObject.getInt("prompt_tokens")
                    val tokens = completionTokens


                    val promptN="1$currentP"
                    val contentN="2$content"
                    editLastIndex(contentN)
                    tokenCal(tokens)
                    Log.e(tag, "Got response:$content")
                    scrollToBottom()
                    promptUpload(promptN, contentN)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun tokenCal(token: Int) {
        if ( token >=150) {
            continueAboveResponsebtn.visibility = View.VISIBLE
        }
    }

    private fun optimizeStringForJson(input: String): String {
        val escapedInput = input
            .replace("\\", "\\\\")  // Escape backslashes
            .replace("\"", "\\\"")  // Escape double quotes
            .replace("\n", "\\n")  // Escape newlines
            .replace("\r", "\\r")  // Escape carriage returns
            .replace("\t", "\\t")  // Escape tabs
            .replace("\\s+".toRegex(), " ")
            .removePrefix("1")
            .removePrefix("2")
        val unicodeEscapedInput = StringBuilder()
        for (c in escapedInput) {
            if (c.toInt() < 128) {
                unicodeEscapedInput.append(c)
            } else {
                unicodeEscapedInput.append("\\u").append(String.format("%04X", c.toInt()))
            }
        }

        return unicodeEscapedInput.toString()
    }

    private fun recyclerViewAdd() {
//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        // Create and set the adapter for the RecyclerView
//        adapter = ChatAdapter(message)
//        recyclerView.adapter = adapter
//
//        // Add sample messages to the adapte
//        adapter.submitList(message)
        scrollToBottom()
    }

    private fun historyDownload() {
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
                    if (p1 == "null") {
                        p1 = ""
                    }
                    if (p2 == "null") {
                        p2 = ""
                    }
                    if (p3 == "null") {
                        p3 = ""
                    }
                    if (a1 == "null") {
                        a1 = ""
                    }
                    if (a2 == "null") {
                        a2 = ""
                    }
                    if (a3 == "null") {
                        a3 = ""
                    }
                    if (currentNumber == "1") {
                        message.add(p1)
                        message.add(a1)
                        message.add(p2)
                        message.add(a2)
                        message.add(p3)
                        message.add(a3)
                    } else if (currentNumber == "2") {
                        message.add(p3)
                        message.add(a3)
                        message.add(p2)
                        message.add(a2)
                        message.add(p1)
                        message.add(a1)
                    } else if (currentNumber == "3") {
                        message.add(p3)
                        message.add(a3)
                        message.add(p1)
                        message.add(a1)
                        message.add(p2)
                        message.add(a2)
                    }
                    recyclerViewAdd()
                }


            }
    }
    private fun updateRview(content:String){
//       message.add(content) // Add the new message to the mutable list
        adapter.addItem(content)

    }
    private fun editLastIndex(content:String){
        // Get the index of the last element in the mutable list
        val lastIndex = message.lastIndex

// Replace the last element with the new element
        message[lastIndex] = content

// Notify the RecyclerView adapter that the data set has changed
        adapter.notifyItemChanged(lastIndex)


    }
    private fun closeKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun scrollToBottom(){
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        // Create and set the adapter for the RecyclerView
        adapter = ChatAdapter(message)
        recyclerView.adapter = adapter
        adapter.submitList(message)
        // Add sample messages to the adapte
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun openMenu(view: View) {
        startActivity(Intent(this,MenuActivity::class.java))
    }

    private fun openSessionBottomSheet() {
       launchBottomSheet()
    }

    fun openBottomSheetSecondaryAction(view: View) {
       launchBottomSheet()
    }
    fun loadChats(sessionId:String){
        pInfoLocation = db.collection(userId).document(sessionId)
        sidLocation = db.collection(userId).document(sessionId).collection("context").document("chatContext")
        getNum()
    }
    private fun launchBottomSheet (){
        var recyclerViewBottomSheet:RecyclerView
        val dialog=BottomSheetDialog(this)
         rootView = layoutInflater.inflate(R.layout.fragment_bottom_sheet,null)
        val titleTextView = rootView.findViewById<TextView>(R.id.text_title)
        startChatBtn=rootView.findViewById(R.id.buttonStartChat)
        auth=Firebase.auth
        userId= auth.currentUser?.uid.toString()
        closeImageViewBtn=rootView.findViewById(R.id.closeImageViewButton)
        sessionNameEditText=rootView.findViewById(R.id.sessionNameEditText)
        newChat=rootView.findViewById(R.id.newChatLayout)
        sessionNameOutBox=rootView.findViewById(R.id.sessionNameOutBox)
        recyclerViewBottomSheet=rootView.findViewById(R.id.bottomSheetRecyclerView)
        sessionNameOutBox.visibility=View.GONE
        sessionNameEditText.visibility=View.GONE
        startChatBtn.visibility=View.GONE
        newChat.setOnClickListener {
            titleTextView.visibility=View.GONE
            recyclerViewBottomSheet.visibility=View.GONE
            newChat.visibility=View.GONE
            sessionNameOutBox.visibility=View.VISIBLE
            sessionNameEditText.visibility=View.VISIBLE
            startChatBtn.visibility=View.VISIBLE
        }

        recyclerViewBottomSheet = rootView.findViewById(R.id.bottomSheetRecyclerView)
        sessionAdapter = SessionAdapter(sessions) { sessionId ->
            loadChats(sessionId)
            dialog.dismiss()
            // Handle click event and use the session ID
            // Here, you can open a new activity or perform any other action
        }
        recyclerViewBottomSheet.adapter = sessionAdapter
        recyclerViewBottomSheet.layoutManager = LinearLayoutManager(this)
        val sessionRef=db.collection(userId)
        sessionRef.get()
            .addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val sessionId = document.getString("sessionId")
                val sessionName = document.getString("sessionName")
                if (sessionId != null && sessionName != null) {
                    val session = Session(sessionId, sessionName)
                    sessions.add(session)
                }
            }
            sessionAdapter.notifyDataSetChanged()
        }

        startChatBtn.setOnClickListener {
            //Store chat name to
            val sessionName = sessionNameEditText.text.toString()
            val sessionId=getTimeInMillis().toString()
            val data = hashMapOf("sessionName" to sessionName,
                "Num" to "1",
                "sessionId" to sessionId,
                "chatNumber" to 0)
            val docRef=db.collection(userId).document(sessionId)
            docRef.set(data)
                .addOnSuccessListener {
                    loadChats(sessionId)
                        dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(MainActivity(),"Failed to start the Chat due to $it",Toast.LENGTH_LONG).show()
                }
        }
        dialog.setCancelable(false)

        // on below line we are setting
        // content view to our view.
        dialog.setContentView(rootView)

        // on below line we are calling
        // a show method to display a dialog.
        dialog.show()
    }
    private fun getTimeInMillis():Long{
        return Calendar.getInstance().timeInMillis
    }

}
class SessionAdapter(
    private val sessions: List<Session>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sessionNameTextView: TextView = itemView.findViewById(R.id.sessionName)

        fun bind(session: Session) {
            sessionNameTextView.text = session.sessionName
            itemView.setOnClickListener { onItemClick(session.sessionId) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.session_item,
            parent,
            false
        )
        return SessionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.bind(session)
    }

    override fun getItemCount(): Int {
        return sessions.size
    }
}
data class Session(
    val sessionId:String,
    val sessionName:String
)