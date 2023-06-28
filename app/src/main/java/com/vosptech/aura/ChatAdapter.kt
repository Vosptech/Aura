package com.vosptech.aura

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter (private val messageList: MutableList<String>) : ListAdapter<String, ChatAdapter.MessageViewHolder>(MessageDiffCallback()) {
    var state = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)

    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    fun addItem(message: String) {
        messageList.add(message)
        notifyItemInserted(messageList.size - 1)
    }


    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val messageOutBox:LinearLayout = itemView.findViewById(R.id.messageOutbox)
        private val loadingGif:ImageView=itemView.findViewById(R.id.loadingGif)
        private val outerMostLayout:LinearLayout=itemView.findViewById(R.id.outerMostLayout)

        fun bind(message: String) {
            var num=""
            if (message!="") {
                 num = message[0].toString()
            }
            if (num=="1") {
                val nMessage=message.removePrefix("1")
                messageOutBox.gravity = Gravity.END
                state = 0
                updateMargin(140, 10, 5, 5)
                updateLayoutGravity(Gravity.END)
                messageOutBox.setBackgroundResource(R.drawable.chat_bg2)
                messageTextView.text = nMessage
                loadingGif.visibility=View.GONE
            } else if (num == "2") {
                val nMessage=message.removePrefix("2")
                messageOutBox.gravity = Gravity.START
                state = 1
                updateLayoutGravity(Gravity.START)
                updateMargin(5, 10, 140, 5)
                messageOutBox.setBackgroundResource(R.drawable.chat_message_background)
                messageTextView.text = nMessage
                if (nMessage=="Please wait..."){
                    loadingGif.visibility=View.VISIBLE
                }else{
                    loadingGif.visibility=View.GONE
                }
            }else if(num==""){
                outerMostLayout.visibility=View.GONE
                loadingGif.visibility=View.GONE
                messageTextView.visibility=View.GONE
                messageOutBox.visibility=View.GONE
            }



        }
        private fun updateMargin(leftMargin: Int, topMargin: Int, rightMargin: Int, bottomMargin: Int) {
            val layoutParams = messageOutBox.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            messageOutBox.layoutParams = layoutParams
        }
        private fun updateLayoutGravity(gravity: Int) {
            val layoutParams = messageOutBox.layoutParams as LinearLayout.LayoutParams
            layoutParams.gravity = gravity
            messageOutBox.layoutParams = layoutParams
        }


    }

    class MessageDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

}