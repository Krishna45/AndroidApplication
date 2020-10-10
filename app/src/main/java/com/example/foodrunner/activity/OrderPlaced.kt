package com.example.foodrunner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.foodrunner.R

class OrderPlaced : AppCompatActivity() {

    lateinit var btnConfirm:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_placed)
        btnConfirm=findViewById(R.id.btnConfirm)
        btnConfirm.setOnClickListener{
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    override fun onBackPressed() {

    }
}