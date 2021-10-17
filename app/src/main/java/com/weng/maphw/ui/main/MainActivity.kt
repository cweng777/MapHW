package com.weng.maphw.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.weng.maphw.databinding.ActivityMainBinding
import com.weng.maphw.ui.map.MapsActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var startForSignInResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeGoogleSignInVariables()
        bindEvents()
    }

    private fun initializeGoogleSignInVariables() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // GoogleSignInOptions的builder藉由 DEFAULT_SIGN_IN創建可以用來要求使用者Email等基本資訊的GoogleSignInOptions實體
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        // 從 GoogleSignInOptions實體 創 GoogleSignInClient, GoogleSignInClient可用來登入 和 登出  with google
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        //set up startActivityForResult for google sign in
        //設置Activity結束後, 如何處理來的 launch參數, 準備而已, 要launch才會執行
        startForSignInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                //如果activity結束後, result code ok, 把回傳的使用者資料從result intent拿出放入Task裡給handleSignInResult這方法來處理
                // The Task returned from this call is always completed, no need to attach a listener.
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent( //Task: Represents an asynchronous operation, await
                    it.data
                )
                handleSignInResult(task)
            }
        }
    }

    private fun bindEvents() {
        binding.signInButton.setOnClickListener {
            signIn()
        }
        binding.signOutButton.setOnClickListener {
            signOut()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent //得到google sign in 的 intent
        startForSignInResult.launch(signInIntent) //做startActivityForResult, launch這個intent, 將會開啟google設定好的activity取使用者帳號
    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(this@MainActivity, "Sign out successfully", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            //取得使用者資訊
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("acctInfo", "acctInfo=> email:${account.email}" +
                    " personId: ${account.displayName}" +
                    " personId: ${account.id}")
            // Signed in successfully, show authenticated UI, 進下一頁(MapsActivity)
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(intent)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.statusCode)
        }
    }
}