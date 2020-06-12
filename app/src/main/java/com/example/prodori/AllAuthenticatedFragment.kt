package com.example.prodori

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_all_authenticated.view.*

class AllAuthenticatedFragment : Fragment() {

    private val posts = ArrayList<PostData>()
    private lateinit var mContext: Context
    private var adapter: PostSearchAdapter? = null
    private lateinit var cfm: FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_authenticated, container, false)

        val postDatabase = FirebaseDatabase.getInstance().getReference("posts")
        postDatabase.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(mContext, "게시글 로드 실패", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.children) {
                    val postMap = snapshot.value as HashMap<*, *>

                    Log.i(".AllAuthFragment", postMap["title"] as String)
                    Log.i(".AllAuthFragment", postMap["writer"] as String)
                    Log.i(".AllAuthFragment", postMap["content"] as String)
                    Log.i(".AllAuthFragment", postMap["category"] as String)

                    posts.add(PostData(postMap["title"] as String, postMap["writer"] as String, postMap["content"] as String, postMap["category"] as String))

                    var updated = false
                    while(!updated) {
                        if(adapter != null) {
                            Log.i("dataChanged", "updating..")
                            adapter!!.notifyDataSetChanged()
                            updated = true
                        }
                    }
                }
            }
        })

        view.postRecyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        adapter = PostSearchAdapter(mContext, posts)
        view.postRecyclerView.adapter = adapter

        view.postImageView.setOnClickListener {
            val fragmentTransaction = cfm.beginTransaction()
            val fragment = WritePostFragment()
            fragment.setFragmentManager(cfm)
            fragmentTransaction.replace(R.id.dynamicFragment, fragment)
            fragmentTransaction.commit()
        }
        view.reviewCategory.setOnClickListener {
            adapter!!.curPosts.clear()
            for(post in posts) {
                if(post.category == "사용 후기")
                    adapter!!.curPosts.add(post)
            }
            adapter!!.notifyDataSetChanged()
        }
        view.questionCategory.setOnClickListener {
            adapter!!.curPosts.clear()
            for(post in posts) {
                if(post.category == "질문")
                    adapter!!.curPosts.add(post)
            }
            adapter!!.notifyDataSetChanged()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    fun setFragmentManager(cfm: FragmentManager) {
        this.cfm = cfm
    }
}
