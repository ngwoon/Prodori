package com.example.prodori

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.lang.ref.WeakReference


class SearchFragment : Fragment() {

    private lateinit var mContext: Context
    private val RESPONSE_CODE = ArrayList<Pair<String, String>>()
    private val searchResult = ArrayList<AdInfo>()
    private lateinit var navController: NavController

    init {
        RESPONSE_CODE.add(Pair("01", "APP_ERROR"))
        RESPONSE_CODE.add(Pair("02", "DB_ERROR"))
        RESPONSE_CODE.add(Pair("03", "NO_DATA"))
        RESPONSE_CODE.add(Pair("04", "HTTP_ERROR"))
        RESPONSE_CODE.add(Pair("05", "TIME_OUT"))
        RESPONSE_CODE.add(Pair("10", "NO_SERVICE_KEY"))
        RESPONSE_CODE.add(Pair("11", "NO_NECESSARY_PARAMS"))
        RESPONSE_CODE.add(Pair("12", "WRONG_URL"))
        RESPONSE_CODE.add(Pair("20", "APPROVE_ERROR"))
        RESPONSE_CODE.add(Pair("22", "EXCEED_USAGE"))
        RESPONSE_CODE.add(Pair("30", "WRONG_SERVICE_KEY"))
        RESPONSE_CODE.add(Pair("31", "WRONG_IP"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_search, container, false)

        view.searchButton.setOnClickListener {
            val searchText = view.editText.text.toString()
            if(searchText == "") {
                Toast.makeText(mContext, "검색어를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                view.editText.clearFocus()
            } else {
                val asyncTask = RequestAsyncTask(this)
                asyncTask.execute(searchText)
                view.tableLayout.removeAllViews()
            }
            
            // 검색 버튼 클릭 시 소프트키보드 숨기기
            val imm: InputMethodManager? = mContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow((mContext as Activity).currentFocus?.windowToken, 0)
        }

        view.helpImageView.setOnClickListener {
            val builder = AlertDialog.Builder(mContext)
            builder.setView(layoutInflater.inflate(R.layout.dialog, null))
                .setPositiveButton("확인") { _, _ -> }
                .setTitle("도움말")
                .setMessage(R.string.search_help_info)
                .show()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        val host = (mContext as MainActivity).supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navController = host.navController
    }

    override fun onResume() {
        super.onResume()
        makeRecord(null)
    }
    
    // searchResult 안의 pName들 겹치는 내용 걸러내기
    private fun pullOutProductName(searchWord: String) {
        for(adinfo in searchResult) {
            val refinedProductName = adinfo.pName.split(",")
            if(refinedProductName.isNotEmpty()) {
                adinfo.pName = ""
                for(pName in refinedProductName) {
                    if(searchWord in pName && pName !in adinfo.pName)
                        adinfo.pName += "$pName\n"
                }
            }
            adinfo.pName = adinfo.pName.trim()
        }
    }

    private fun makeRecord(searchWord: String?) {

        // onResume에서 호출될 때에는 이미 searchResult가 refine된 상태이므로 아래 작업을 할 필요가 없다.
        if(searchWord != null)
            pullOutProductName(searchWord)

        val rowParams = TableRow.LayoutParams(TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f))
        val textParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f)

        for(i in 0 until searchResult.size) {
            val row = TableRow(mContext)
            row.layoutParams = rowParams

            row.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("AdInfo", searchResult[i])

                val host = (mContext as MainActivity).supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
                host.navController.navigate(R.id.action_search_to_detail, bundle)
            }

            val textViews = ArrayList<TextView>()
            textViews.add(TextView(mContext))
            textViews.add(TextView(mContext))
            textViews.add(TextView(mContext))

            for(j in 0 until textViews.size) {
                textViews[j].layoutParams = textParams
                textViews[j].background = resources.getDrawable(R.drawable.cell_shape, null)
                textViews[j].textSize = 15.0f
                textViews[j].setPadding(5,5,5,5)
                textViews[j].setTextColor(Color.BLACK)
                textViews[j].gravity = Gravity.CENTER
                row.addView(textViews[j])
            }

            textViews[0].text = searchResult[i].pName
            textViews[1].text = searchResult[i].eName
            textViews[2].text = searchResult[i].date

            view!!.tableLayout.addView(row)
        }
    }

    inner class RequestAsyncTask(context: SearchFragment) : AsyncTask<String, Unit, String>() {
        private val activityReference = WeakReference(context)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = activityReference.get()
            activity?.view!!.progressBar?.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String): String {
            val url = "http://apis.data.go.kr/1470000/FoodFlshdErtsInfoService/getFoodFlshdErtsItem?ServiceKey=" +
                    "${getString(R.string.SERVICE_KEY)}&Prduct=${params[0]}"
            val doc = Jsoup.connect(url).parser(Parser.xmlParser()).get()
            val responseCode = doc.select("resultCode").toString()

            var isSuccess = true
            for(code in RESPONSE_CODE) {
                if(code.first == responseCode) {
                    Toast.makeText(mContext, code.second, Toast.LENGTH_SHORT).show()
                    isSuccess = false
                    break
                }
            }

            if(isSuccess) {
                searchResult.clear()

                val items = doc.select("item")

                for(item in items) {
                    val product = item.select("PRDUCT").text()
                    val enterprise = item.select("ENTRPS").text()
                    val address = item.select("ADRES1").text()
                    val found_cn = item.select("FOUND_CN").text()
                    val date = item.select("DSPS_DT").text()
                    val punishment = item.select("DSPS_CMMND").text()
                    val violation = item.select("VIOLT").text()

                    searchResult.add(AdInfo(product, enterprise, address, found_cn, date, punishment, violation))
                }
            }
            else {

            }
            return params[0]
        }

        override fun onPostExecute(searchWord: String) {
            super.onPostExecute(searchWord)
            val activity = activityReference.get()
            val mainActivity = activity?.mContext as MainActivity

            if(activity.isDetached || mainActivity.isFinishing)
                return

            activity.progressBar.visibility = View.GONE
            makeRecord(searchWord)
        }
    }
}
