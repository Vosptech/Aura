package com.example.aura

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter : ListAdapter<String, ChatAdapter.MessageViewHolder>(MessageDiffCallback()) {
    var state = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)

    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: String) {
            if (state==1){
                messageTextView.gravity=Gravity.END
                state=0
                updateMargin(140,10,5,5)
                updateLayoutGravity(Gravity.END)
                messageTextView.setBackgroundResource(R.drawable.chat_bg2)
            }else if (state==0){
                messageTextView.gravity=Gravity.START
                state=1
                updateLayoutGravity(Gravity.START)
                updateMargin(5,10,140,5)
                messageTextView.setBackgroundResource(R.drawable.chat_message_background)
            }
            messageTextView.text = message
        }
        private fun updateMargin(leftMargin: Int, topMargin: Int, rightMargin: Int, bottomMargin: Int) {
            val layoutParams = messageTextView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            messageTextView.layoutParams = layoutParams
        }
        private fun updateLayoutGravity(gravity: Int) {
            val layoutParams = messageTextView.layoutParams as LinearLayout.LayoutParams
            layoutParams.gravity = gravity
            messageTextView.layoutParams = layoutParams
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