package com.example.prodori

import android.content.Context
import android.os.Bundle
import android.util.Log
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
        Log.i("CommunityFragment", "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        checkAuth()
    }

    private fun checkAuth() {
        Log.i("CommunityFragment", "checkAuth")
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val fragment: Fragment =
            if(!LoginInfo.isLoggedIn)
                NotAuthenticatedFragment()
            else if(LoginInfo.nickname == LoginInfo.NO_NICKNAME) {
                Log.i("CommunityFragment", "no nickname")
                NoNicknameFragment()
            }
            else {
                Log.i("CommunityFragment", "allauth")
                AllAuthenticatedFragment()
            }

        fragmentTransaction.replace(R.id.dynamicFragment, fragment)
        fragmentTransaction.commit()
    }

    override fun onAttach(context: Context) {
        Log.i("CommunityFragment", "onattach")
        super.onAttach(context)
        mContext = context
//        val host = (mContext as MainActivity).supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
//        navController = host.navController
    }

    fun refresh() {
        checkAuth()
    }
}
