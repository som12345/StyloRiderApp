package com.example.styloriderapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

abstract class StyloBaseActivity : AppCompatActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContentView(getLayoutResourceID())
    }
    abstract fun getLayoutResourceID() :Int
}

