package com.example.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.foodrunner.R
import com.example.foodrunner.database.RestaurantDetailDatabase

class Splash : AppCompatActivity() {
    lateinit var pd:ProgressBar
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        sharedPreferences=getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        title="FoodRunner"
        var a:Int=0xFDEDEC
        pd=findViewById(R.id.pd);
        pd.indeterminateDrawable.setColorFilter(a,PorterDuff.Mode.SRC_IN);
        Handler().postDelayed({
            if(sharedPreferences.getBoolean("isLoggedIn",false))
            {
                var intent= Intent(this@Splash,
                    MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else
            {
                var intent=Intent(this@Splash,Login::class.java)
                startActivity(intent)
                finish()
            }
        },1000)
        RestaurantDetails.DBRemoveAsyncTask(this).execute().get()
        }
    class DBRemoveAsyncTask(context:Context): AsyncTask<Void, Void, Boolean>(){
        val db= Room.databaseBuilder(context, RestaurantDetailDatabase::class.java,"detail-db").build()
        override fun doInBackground(vararg p0: Void?): Boolean {
            db.restaurantDetailDao().deleteAll()
            return true
        }
    }
}