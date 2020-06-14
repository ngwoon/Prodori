package com.example.prodori

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class CommunityFragment : Fragment() {

    private lateinit var mContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        checkAuth()
    }

    private fun checkAuth() {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val fragment: Fragment =
            if(!LoginInfo.isLoggedIn)
                NotAuthenticatedFragment()
            else if(LoginInfo.nickname == LoginInfo.NO_NICKNAME) {
                val nnf = NoNicknameFragment()
                nnf.setFragmentManager(childFragmentManager)

                nnf
            }
            else {
                val aaf = AllAuthenticatedFragment()
                aaf.setFragmentManager(childFragmentManager)

                aaf
            }

        fragmentTransaction.replace(R.id.dynamicFragment, fragment)
        fragmentTransaction.commit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    fun refresh() {
        checkAuth()
    }
}
