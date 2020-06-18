package com.example.prodori

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_search.view.*

class DetailInfoFragment : Fragment() {

    private val itemTitle = arrayOf("물품명", "제조사", "주소", "적발내용", "행정처분일자", "처분명", "위반법령")
    private lateinit var mContext: Context
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail_info, container, false)

        val bundle = arguments
        val data = bundle?.getSerializable("AdInfo")

        makeRecord(data as AdInfo, view)

        (mContext as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
//        val customView = (mContext as MainActivity).supportActionBar?.customView
//        var leftImageView = customView?.findViewById<ImageView>(R.id.leftImageView)
//        leftImageView?.setImageDrawable(resources.getDrawable(R.drawable.ic_goback, null))
//        leftImageView?.setOnClickListener {
//            navController.popBackStack()
//        }

        return view
    }

    private fun makeRecord(data: AdInfo, view: View) {
        val rowParams = TableRow.LayoutParams(TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f))
        val textParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f)

        val dataContent = ArrayList<String>()
        dataContent.add(data.pName)
        dataContent.add(data.eName)
        dataContent.add(data.address)
        dataContent.add(data.found_cn)
        dataContent.add(data.date)
        dataContent.add(data.punishment)
        dataContent.add(data.violation)

        for(i in itemTitle.indices) {
            val titleRow = TableRow(mContext)
            titleRow.layoutParams = rowParams

            val titleTextView = TextView(mContext)
            titleTextView.layoutParams = textParams
            titleTextView.text = itemTitle[i]
            titleTextView.textSize = 20f
            titleTextView.setPadding(5, 5, 5, 5)
            titleTextView.gravity = Gravity.CENTER
            titleTextView.background = resources.getDrawable(R.drawable.cell_shape, null)

            titleRow.addView(titleTextView)


            val contentRow = TableRow(mContext)
            contentRow.layoutParams = rowParams

            val contentTextView = TextView(mContext)
            contentTextView.layoutParams = textParams
            contentTextView.text = dataContent[i]
            contentTextView.textSize = 15f
            contentTextView.setPadding(10, 20, 10, 20)
            contentTextView.background = resources.getDrawable(R.drawable.cell_shape, null)

            contentRow.addView(contentTextView)

            view.tableLayout.addView(titleRow)
            view.tableLayout.addView(contentRow)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        val host = (mContext as MainActivity).supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navController = host.navController
    }
}
