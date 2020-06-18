package com.example.prodori

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_write_post.view.*

class WritePostFragment : Fragment() {

    private lateinit var mContext: Context
    private lateinit var cfm: FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_write_post, container, false)

        // 소프트키보드 활성 시 버튼이 위로 올라오는 상황 방지
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val spinnerAdapter = ArrayAdapter(mContext, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.category))
        view.categorySpinner.adapter = spinnerAdapter

        view.submitButton.setOnClickListener {
            if(view.titleEditText.text.isEmpty())
                Toast.makeText(mContext, "제목을 입력해 주세요", Toast.LENGTH_SHORT).show()
            else if(view.contentEditText.text.isEmpty())
                Toast.makeText(mContext, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show()
            else {
                val category = view.categorySpinner.getItemAtPosition(view.categorySpinner.selectedItemPosition) as String
                val title = view.titleEditText.text.toString()
                val content = view.contentEditText.text.toString()
                val postDatabase = FirebaseDatabase.getInstance().getReference("posts")
                val newPostRef = postDatabase.push()
                newPostRef.setValue(PostData(title, LoginInfo.nickname, content, category, 0, 0, 0))

//                var newPostKey = newPostRef.key
//
//                val replyDatabase = FirebaseDatabase.getInstance().getReference("replys")


                val fragmentTransaction = cfm.beginTransaction()
                val fragment = AllAuthenticatedFragment()
                fragment.setFragmentManager(cfm)
                fragmentTransaction.replace(R.id.dynamicFragment, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
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
