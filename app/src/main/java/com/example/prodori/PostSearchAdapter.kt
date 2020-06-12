package com.example.prodori

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostSearchAdapter(private val mContext: Context, private val posts: ArrayList<PostData>) : RecyclerView.Adapter<PostSearchAdapter.ViewHolder>() {

    val curPosts = ArrayList<PostData>()

    init {
        curPosts.addAll(posts)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val writer: TextView = itemView.findViewById(R.id.writer)
        val category: TextView = itemView.findViewById(R.id.category)
        val postImage: ImageView = itemView.findViewById(R.id.postImageView)
        init {
            itemView.setOnClickListener {
                
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int) : ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.post_preview, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.i("getItemCount", "size : ${curPosts.size}")
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = posts[position].title
        holder.writer.text = posts[position].writer
        holder.category.text = posts[position].category
//        holder.postImage.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_no_image, null))
        holder.writer.text = posts[position].writer
    }
}