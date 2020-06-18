package com.example.prodori

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
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
    private val postKeys = ArrayList<String>()
    lateinit var mContext: Context
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
                posts.clear()
                postKeys.clear()
                for(snapshot in dataSnapshot.children) {
                    postKeys.add(snapshot.key!!)

                    val postMap = snapshot.value as HashMap<*, *>

                    posts.add(PostData(postMap["title"] as String, postMap["writer"] as String, postMap["content"] as String, postMap["category"] as String, postMap["like"] as Long, postMap["unlike"] as Long, postMap["report"] as Long))

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
        adapter = PostSearchAdapter(mContext, posts, postKeys, cfm)
        view.postRecyclerView.adapter = adapter

        view.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter!!.searchCondition = view.searchConditionSpinner.getItemAtPosition(view.searchConditionSpinner.selectedItemPosition) as String
                adapter!!.filter.filter(query)
                view.searchView.setQuery("", false)
                view.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val spinnerAdapter = ArrayAdapter(mContext, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.search_condition))
        view.searchConditionSpinner.adapter = spinnerAdapter

        view.postImageView.setOnClickListener {
            val fragmentTransaction = cfm.beginTransaction()
            val fragment = WritePostFragment()
            fragment.setFragmentManager(cfm)
            fragmentTransaction.replace(R.id.dynamicFragment, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
        view.totalCategory.setOnClickListener {
            adapter!!.curPosts = posts
            adapter!!.notifyDataSetChanged()
        }
        view.reviewCategory.setOnClickListener {
            val filteringList = ArrayList<PostData>()
            for(post in posts) {
                if(post.category == "구매 후기")
                    filteringList.add(post)
            }
            adapter!!.curPosts = filteringList
            adapter!!.notifyDataSetChanged()
        }
        view.questionCategory.setOnClickListener {
            val filteringList = ArrayList<PostData>()
            for(post in posts) {
                if(post.category == "질문")
                    filteringList.add(post)
            }
            adapter!!.curPosts = filteringList
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
