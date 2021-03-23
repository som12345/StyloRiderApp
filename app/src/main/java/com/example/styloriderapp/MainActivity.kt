package com.example.styloriderapp

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : StyloBaseActivity() {

    override fun getLayoutResourceID(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "Login"
        button.setOnClickListener {
            if(editTextPhone.text.toString() == "1234567890" &&
                editTextTextPassword.text.toString() == "stylo") {
                startActivity(Intent(this@MainActivity,
                    HomeActivity::class.java))
            }
        }
    }


}