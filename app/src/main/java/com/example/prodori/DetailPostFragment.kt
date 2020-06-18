package com.example.prodori

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_detail_post.*
import kotlinx.android.synthetic.main.fragment_detail_post.view.*
import java.lang.ref.WeakReference

class DetailPostFragment : Fragment() {

    private val replys = ArrayList<ReplyData>()
    private lateinit var mContext: Context
    lateinit var post: PostData
    lateinit var key: String

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_detail_post, container, false)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        post = arguments!!["postData"] as PostData
        key = arguments!!["postKey"] as String

        view.detail_post_title.text = post.title
        view.detail_post_writer.text = post.writer
        view.detail_post_category.text = post.category
        view.detail_post_content.text = post.content
        view.detail_post_like.text = "추천 " + post.like.toString()
        view.detail_post_unlike.text = "비추천 " + post.unlike.toString()
        view.detail_post_like_button.text = post.like.toString()
        view.detail_post_unlike_button.text = post.unlike.toString()

        val replyDatabase = FirebaseDatabase.getInstance().getReference("replys")
        view.detail_post_replay_submit_button.setOnClickListener {
            if(view.detail_post_reply_content.text.toString() == "")
                Toast.makeText(mContext, "내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            else {
                val newReplyData = ReplyData(LoginInfo.nickname, view.detail_post_reply_content.text.toString())
                replyDatabase.child(key).push().setValue(newReplyData)
                view.detail_post_reply_content.setText("")
                Toast.makeText(mContext, "댓글을 작성했습니다.", Toast.LENGTH_SHORT).show()

                replys.add(newReplyData)

                val newReplyDataToList = ArrayList<ReplyData>()
                newReplyDataToList.add(newReplyData)
                makeReplyView(newReplyDataToList)
            }
        }

        view.detail_post_like_button.setOnClickListener {
            checkAlreadyPressed("like")
        }

        view.detail_post_unlike_button.setOnClickListener {
            checkAlreadyPressed("unlike")
        }

        view.detail_post_siren_button.setOnClickListener {
            val builder = AlertDialog.Builder(mContext)
            builder.setView(layoutInflater.inflate(R.layout.dialog, null))
                .setNegativeButton("취소") { _, _ -> }
                .setPositiveButton("확인") { _, _ ->
                    checkAlreadyPressed("report")
                }
                .setTitle("신고")
                .setIcon(R.drawable.ic_siren)
                .setMessage(R.string.report_warning)
                .show()
        }

        return view
    }

    private fun checkAlreadyPressed(type: String) {
        val userActivityDatabase = FirebaseDatabase.getInstance().getReference("userActivity")
        userActivityDatabase.child(type).orderByKey().equalTo(LoginInfo.nickname).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.value as HashMap<String,String>

                val postKeys = value[LoginInfo.nickname]!!.split(" ")

                var likeAlreadyPressed = false
                for(postKey in postKeys!!) {
                    if(key == postKey) {
                        likeAlreadyPressed = true
                        break
                    }
                }
                if(!likeAlreadyPressed) {

                    value[LoginInfo.nickname] = value[LoginInfo.nickname] + " " + key
                    userActivityDatabase.child(type).child(LoginInfo.nickname).setValue(value[LoginInfo.nickname])

                    val postDatabase = FirebaseDatabase.getInstance().getReference("posts")
                    postDatabase.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val postMap = dataSnapshot.value as HashMap<*,*>
                            post.like = postMap["like"] as Long
                            post.unlike = postMap["unlike"] as Long
                            post.report = postMap["report"] as Long

                            when(type) {
                                "like" -> {
                                    Toast.makeText(mContext, "이 게시글을 추천합니다.", Toast.LENGTH_SHORT).show()
                                    post.like += 1
                                }
                                "unlike" -> {
                                    Toast.makeText(mContext, "이 게시글을 비추천합니다.", Toast.LENGTH_SHORT).show()
                                    post.unlike += 1
                                }
                                "report" -> {
                                    Toast.makeText(mContext, "이 게시글을 신고합니다.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            postDatabase.child(key).setValue(post)

                            view!!.detail_post_like_button.text = post.like.toString()
                            view!!.detail_post_like.text = "추천 " + post.like.toString()
                            view!!.detail_post_unlike_button.text = post.unlike.toString()
                            view!!.detail_post_unlike.text = "비추천 " + post.unlike.toString()
                        }
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(mContext, "데이터베이스 오류. 게시글 추천 실패", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                else {
                    when(type) {
                        "like" -> Toast.makeText(mContext, "이미 추천한 게시글입니다.", Toast.LENGTH_SHORT).show()
                        "unlike" -> Toast.makeText(mContext, "이미 비추천한 게시글입니다.", Toast.LENGTH_SHORT).show()
                        "report" -> Toast.makeText(mContext, "이미 신고한 게시글입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val asyncTask = ReplyAsyncTask(this)
        asyncTask.execute(key)
    }

    private fun makeReplyView(replyData: ArrayList<ReplyData>) {
        val linearParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val textParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val lineParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        lineParam.topMargin = 8

        Log.i("DetailPostFragment_reply", "before reply attach")
        for(reply in replyData) {
            Log.i("DetailPostFragment_reply", reply.writer + " " + reply.content)
            val linearLayout =  LinearLayout(mContext)
            val writerTextView = TextView(mContext)
            val contentTextView = TextView(mContext)
            val line = View(mContext)

            linearLayout.orientation = LinearLayout.VERTICAL

            writerTextView.text = reply.writer
            writerTextView.textSize = 15.0f
            writerTextView.typeface = Typeface.DEFAULT_BOLD
            writerTextView.setPadding(5)

            contentTextView.text = reply.content
            contentTextView.textSize = 15.0f
            contentTextView.setPadding(5)

            line.background = mContext.resources.getDrawable(R.drawable.gradient, null)

            linearLayout.addView(writerTextView, textParam)
            linearLayout.addView(contentTextView, textParam)
            linearLayout.addView(line, lineParam)

            view!!.rootLinearLayout.addView(linearLayout, linearParam)
        }
        view!!.invalidate()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    inner class ReplyAsyncTask(context: DetailPostFragment) : AsyncTask<String, Unit, String>() {
        private val activityReference = WeakReference(context)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = activityReference.get()
            activity?.view!!.detail_post_progressBar?.visibility = View.VISIBLE
            activity?.view!!.rootLinearLayout?.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String): String {
            var replyUpdated = false
            val replyDatabase = FirebaseDatabase.getInstance().getReference("replys")
            replyDatabase.child(params[0])
            replyDatabase.child(params[0]).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    replys.clear()
                    for(snapshot in dataSnapshot.children) {
                        val replyMap = snapshot.value as HashMap<*,*>
                        replys.add(ReplyData(replyMap["writer"] as String, replyMap["content"] as String))
                    }
                    replyUpdated = true
                }
            })

            while(!replyUpdated)
                Log.i("while", "reply waiting...")

            return params[0]
        }

        override fun onPostExecute(key: String) {
            super.onPostExecute(key)
            val activity = activityReference.get()
            val mainActivity = activity?.mContext as MainActivity

            if(activity.isDetached || mainActivity.isFinishing)
                return

            makeReplyView(replys)
            activity.detail_post_progressBar.visibility = View.GONE
            activity.rootLinearLayout.visibility = View.VISIBLE
        }
    }
}
