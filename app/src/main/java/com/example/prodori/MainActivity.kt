package com.example.prodori

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var currentNavController: LiveData<NavController>? = null
    private val SIGN_IN_REQUEST_CODE = 100
    private var savedInstanceState: Bundle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.savedInstanceState = savedInstanceState
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

        // 왼쪽 상단 imageView 이미지 지정
        val leftImageView = findViewById<ImageView>(R.id.leftImageView)
        leftImageView.setImageDrawable(
            when(LoginInfo.isLoggedIn) {
                true -> resources.getDrawable(R.drawable.ic_logout, null)
                false -> resources.getDrawable(R.drawable.ic_login, null)
            }
        )

        // 로그인, 로그아웃 리스너
        leftImageView.setOnClickListener {
            if(!LoginInfo.isLoggedIn)
                launchSignInFlow()
            else {
                val builder = AlertDialog.Builder(this)
                builder.setView(layoutInflater.inflate(R.layout.dialog_logout, null))
                    .setNegativeButton("취소") { _, _ -> }
                    .setPositiveButton("확인") { _, _ ->
                        leftImageView.setImageDrawable(resources.getDrawable(R.drawable.ic_login, null))
                        LoginInfo.isLoggedIn = false
                        LoginInfo.email = LoginInfo.DEFAULT_EMAIL
                        LoginInfo.nickname = LoginInfo.NO_NICKNAME
                        FirebaseAuth.getInstance().signOut()
                    }
                    .setTitle("로그아웃")
                    .setMessage(R.string.logout_info)
                    .show()
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch the sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE.
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_main_64px)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in.

                LoginInfo.isLoggedIn = true
                LoginInfo.email = response?.user!!.email!!
                val leftImageView = findViewById<ImageView>(R.id.leftImageView)
                leftImageView.setImageDrawable(resources.getDrawable(R.drawable.ic_logout, null))

                val database = FirebaseDatabase.getInstance().getReference("users")
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@MainActivity, "데이터 로드 실패", Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.

                        val user = response.user!!
                        var isExist = false
                        for(snapshot in dataSnapshot.children) {
                            val pair = snapshot.value as HashMap<*,*>
                            if(pair["email"] == user.email!!) {
                                LoginInfo.key = snapshot.key as String
                                LoginInfo.nickname = pair["nickname"] as String
                                isExist = true
                                break
                            }
                        }
                        if(!isExist) {
                            val newRef = database.push()
                            val valueMap = HashMap<String, String>()
                            valueMap["email"] = user.email!!
                            valueMap["nickname"] = LoginInfo.NO_NICKNAME

                            LoginInfo.key = newRef.key as String
                            newRef.setValue(valueMap)
                        }

                        // 현재 페이지가 community이면 갱신
                        if(bottomNavigationView.selectedItemId == R.id.community) {
                            val hostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container)!!
                            val communityFragment = hostFragment.childFragmentManager.fragments[0] as CommunityFragment
                            communityFragment.refresh()
                        }
                    }
                })

            } else {
                // Sign in failed. If response is null, the user canceled the
                // sign-in flow using the back button. Otherwise, check
                // the error code and handle the error.
                Toast.makeText(this, "Sign in unsuccessful ${response?.error?.errorCode}", Toast.LENGTH_SHORT).show();
            }
        }
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

