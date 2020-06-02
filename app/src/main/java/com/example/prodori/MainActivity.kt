package com.example.prodori

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // savedInstanceState가 null인 경우 = 앱 처음 시작
        // 즉 앱 처음 시작 시 하단 내비게이션 바를 세팅한다.
        if(savedInstanceState == null)
            setUpBottomNavigationBar()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBar = inflater.inflate(R.layout.custom_action_bar, null)

        supportActionBar?.customView = actionBar

        // 액션 바 양측 공백 없애기
        val parent = actionBar.parent as Toolbar
        parent.setContentInsetsAbsolute(0,0)


        return super.onCreateOptionsMenu(menu)
    }

    // onStart() 다음에 호출되는 함수
    // 앱 처음 시작이 아니면, savedInstanceState는 null이 아니게 되고, 이 함수가 호출된다.
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setUpBottomNavigationBar()
    }

    private fun setUpBottomNavigationBar(){
        bottomNavigationView = findViewById(R.id.bottom_nav)
        val navGraphIds = listOf(R.navigation.navigation_search_product, R.navigation.navigation_community, R.navigation.navigation_developer)

        // setupWithNavController가 bottomNavigationView와 navigation 폴더 내의 xml 파일들을 이어준다. (프래그먼트 선 잇기)
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds, supportFragmentManager, R.id.nav_host_container, intent
        )

        controller.observe(this, Observer {navController->
            // 현재 보이고 있는 Fragment의 title을 actionbar에 표시해주고, Navigation Controller와 actionbar를 이어준다.
            // observer를 붙여 navigationController에 변화가 있을 때마다 ActionBar를 설정하게 한다.
            setupActionBarWithNavController(navController)
        })
        currentNavController = controller
    }

    // actionbar에서 뒤로 가기 버튼을 눌렀을 때 호출되는 함수
    // 뒤로 갈 수 있으면 true, 없으면 false를 리턴한다.
    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
}

