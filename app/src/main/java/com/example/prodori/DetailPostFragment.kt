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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
                replyDatabase.child(key).push().setValue(ReplyData(LoginInfo.nickname, view.detail_post_reply_content.text.toString()))
                view.detail_post_reply_content.setText("")
                Toast.makeText(mContext, "댓글을 작성했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val asyncTask = ReplyAsyncTask(this)
        asyncTask.execute(key)
    }

    private fun makeReplyView() {
        val linearParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val textParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val lineParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        lineParam.topMargin = 8

        Log.i("DetailPostFragment_reply", "before reply attach")
        for(reply in replys) {
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
            replyDatabase.child(params[0]).addValueEventListener(object : ValueEventListener {
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

            makeReplyView()
            activity.detail_post_progressBar.visibility = View.GONE
            activity.rootLinearLayout.visibility = View.VISIBLE
        }
    }
}
