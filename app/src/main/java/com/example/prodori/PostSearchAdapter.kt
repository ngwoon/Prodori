package com.example.prodori

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

class PostSearchAdapter(private val mContext: Context, private val posts: ArrayList<PostData>, private val postKeys: ArrayList<String>, private val cfm: FragmentManager) : RecyclerView.Adapter<PostSearchAdapter.ViewHolder>(),
    Filterable {

    var curPosts = ArrayList<PostData>()
    var curPostKeys = ArrayList<String>()
    var searchCondition: String = "제목"

    init {
        curPosts = posts
        curPostKeys = postKeys
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val writer: TextView = itemView.findViewById(R.id.writer)
        val category: TextView = itemView.findViewById(R.id.category)
        val postImage: ImageView = itemView.findViewById(R.id.postImageView)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        init {
            linearLayout.setOnClickListener {
                val fragmentTransaction = cfm.beginTransaction()
                val fragment = DetailPostFragment()
                fragment.arguments = Bundle().apply {
                    putSerializable("postData", curPosts[adapterPosition])
                    putSerializable("postKey", curPostKeys[adapterPosition])
                }
                fragmentTransaction.replace(R.id.dynamicFragment, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int) : ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.post_preview, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.i("getItemCount", "size : ${curPosts.size}")
        return curPosts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = curPosts[position].title
        holder.writer.text = curPosts[position].writer
        holder.category.text = curPosts[position].category
        holder.postImage.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_no_image, null))
        holder.writer.text = curPosts[position].writer
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    curPosts = posts
                } else {
                    val filteringList = ArrayList<PostData>()
                    val keyFilteringList = ArrayList<String>()
                    for(post in posts) {
                        //이부분에서 원하는 데이터를 검색할 수 있음
                        when (searchCondition) {
                            "제목" -> {
                                if(post.title.contains(charSequence)) {
                                    filteringList.add(post)
                                    keyFilteringList.add(postKeys[posts.indexOf(post)])
                                }
                            }
                            "내용" -> {
                                if(post.content.contains(charSequence)) {
                                    filteringList.add(post)
                                    keyFilteringList.add(postKeys[posts.indexOf(post)])
                                }
                            }
                            "작성자" -> {
                                if(post.writer.contains(charSequence)) {
                                    filteringList.add(post)
                                    keyFilteringList.add(postKeys[posts.indexOf(post)])
                                }
                            }
                            "제목 + 내용" -> {
                                if(post.title.contains(charSequence) || post.content.contains(charSequence)) {
                                    filteringList.add(post)
                                    keyFilteringList.add(postKeys[posts.indexOf(post)])
                                }
                            }
                        }
                    }
                    curPosts = filteringList
                    curPostKeys = keyFilteringList
                }

                val filterResults = FilterResults()
                filterResults.values = curPosts
                return filterResults
            }
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                curPosts = filterResults.values as ArrayList<PostData>
                notifyDataSetChanged()
            }
        }
    }
}