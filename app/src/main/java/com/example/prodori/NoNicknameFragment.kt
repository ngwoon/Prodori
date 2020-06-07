package com.example.prodori

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_no_nickname.view.*

class NoNicknameFragment : Fragment() {

    lateinit var mContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_no_nickname, container, false)

        view.useNickname.setOnClickListener {
            val inputNickname = view.inputNickname.text.toString()

            val database = FirebaseDatabase.getInstance().getReference("users")
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(mContext, "데이터 로드 실패", Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    var isExist = false
                    for(snapshot in dataSnapshot.children) {
                        val pair = snapshot.value as HashMap<*,*>
                        if(pair["second"] == inputNickname) {
                            isExist = true
                            break
                        }
                    }

                    if(!isExist) {
                        val map = HashMap<String, Any>()
                        val valueMap = HashMap<String, String>()
                        valueMap["email"] = LoginInfo.email
                        valueMap["nickname"] = inputNickname
                        map[LoginInfo.key] = valueMap
                        database.updateChildren(map)
                    } else
                        Toast.makeText(mContext, "이미 존재하는 닉네임입니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
//        val host = (mContext as MainActivity).supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
//        navController = host.navController
    }

}
