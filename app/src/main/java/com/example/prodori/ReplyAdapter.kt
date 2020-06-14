package com.example.prodori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReplyAdapter(private val replys: ArrayList<ReplyData>) : RecyclerView.Adapter<ReplyAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val writer = itemView.findViewById<TextView>(R.id.reply_writer)
        val content = itemView.findViewById<TextView>(R.id.reply_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.reply, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return replys.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.writer.text = replys[position].writer
        holder.content.text = replys[position].content
    }
}