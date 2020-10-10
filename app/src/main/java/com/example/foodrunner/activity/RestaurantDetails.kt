package com.example.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.R
import com.example.foodrunner.adapter.RestaurantDetailsRecyclerAdapter
import com.example.foodrunner.database.RestaurantDetailDatabase
import com.example.foodrunner.database.RestaurantDetailEntity
import com.example.foodrunner.model.Details
import com.example.foodrunner.model.Restaurant
import com.example.foodrunner.util.ConnectionManager

class RestaurantDetails(

) : AppCompatActivity() {

    lateinit var progressLayout:RelativeLayout
    lateinit var progressBar:ProgressBar
    lateinit var recyclerItems:RecyclerView
    lateinit var layoutManager:RecyclerView.LayoutManager
    lateinit var restaurantAdapter:RestaurantDetailsRecyclerAdapter
    lateinit var addToCart:Button
    var restaurantDetailsList=ArrayList<Details>()
    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    var restaurantId:String?="0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_details)
        recyclerItems=findViewById(R.id.recyclerItems)
        progressLayout=findViewById(R.id.progressLayout)
        progressBar=findViewById(R.id.progressBar)
        toolbar=findViewById(R.id.toolbar)
        addToCart=findViewById(R.id.addToCart)
        setUpToolBar()
        progressLayout.visibility= View.VISIBLE
        addToCart.visibility=View.GONE
        layoutManager=LinearLayoutManager(this@RestaurantDetails)
        if(intent!=null)
        {
            restaurantId=intent.getStringExtra("restaurant_id")
        }
        else
        {
            finish()
            Toast.makeText(this@RestaurantDetails,"Some Unexpected Error occurred",Toast.LENGTH_SHORT).show()
        }
        if(restaurantId=="0")
        {
            finish()
            Toast.makeText(this@RestaurantDetails,"Some Unexpected Error occurred",Toast.LENGTH_SHORT).show()
        }
        println("restaurantId"+restaurantId)
        val queue= Volley.newRequestQueue(this@RestaurantDetails)
        val url="http://13.235.250.119/v2/restaurants/fetch_result/${restaurantId}"
        if(ConnectionManager().checkConnectivity(this@RestaurantDetails))
        {
            val jsonObjectTequest=object: JsonObjectRequest(Request.Method.GET,url,null, com.android.volley.Response.Listener {
                println("Obtained $it")
                try{
                    val response=it.getJSONObject("data")
                    val success=response.getBoolean("success")
                    println("success"+success)
                    if(success){
                        progressLayout.visibility=View.GONE
                        val data = response.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val resJSONObject = data.getJSONObject(i)
                            val resObject = Details(
                                resJSONObject.getString("id"),
                                resJSONObject.getString("name"),
                                resJSONObject.getString("cost_for_one"),
                                resJSONObject.getString("restaurant_id")
                            )

                            restaurantDetailsList.add(resObject)
                            if(this!=null)
                            {
                                restaurantAdapter= RestaurantDetailsRecyclerAdapter(this@RestaurantDetails,restaurantDetailsList,addToCart)
                                recyclerItems.layoutManager=layoutManager
                                recyclerItems.adapter=restaurantAdapter
                                recyclerItems.itemAnimator=DefaultItemAnimator()
                                recyclerItems.setHasFixedSize(true)
                            }
                        }
                    }
                }
                catch (e:Exception)
                {
                     Toast.makeText(this@RestaurantDetails,"Some error occurres",Toast.LENGTH_SHORT).show()
                }


            },com.android.volley.Response.ErrorListener{
               Toast.makeText(this@RestaurantDetails,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
            }
            ){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers=HashMap<String,String>()
                    headers["Content-type"]="application/json"
                    headers["token"]="6061989b41e55e"
                    return headers
                }
            }
            queue.add(jsonObjectTequest)
        }
        else{
            val dialog=AlertDialog.Builder(this@RestaurantDetails)
            dialog.setTitle("Error")
            dialog.setMessage("Internet connection is not found")
            dialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit"){text,listener->
                ActivityCompat.finishAffinity(this@RestaurantDetails)
            }
            dialog.create()
            dialog.show()
        }
        addToCart.setOnClickListener{
             val intent=Intent(this@RestaurantDetails,Cart::class.java)
             startActivity(intent)
        }
    }
    fun setUpToolBar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.title=intent.getStringExtra("restaurant_name")
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        if(id==android.R.id.home)
        {
            onBackPressed()
        }
        DBRemoveAsyncTask(this).execute().get()
        return super.onOptionsItemSelected(item)
    }
    class DBRemoveAsyncTask(context:Context):AsyncTask<Void,Void,Boolean>(){
        val db= Room.databaseBuilder(context,RestaurantDetailDatabase::class.java,"detail-db").build()
        override fun doInBackground(vararg p0: Void?): Boolean {
           db.restaurantDetailDao().deleteAll()
            return true
        }
    }
    class DBGetAllAsyncTask(context:Context):AsyncTask<Void,Void,List<RestaurantDetailEntity>>(){
        val db=Room.databaseBuilder(context,RestaurantDetailDatabase::class.java,"detail-db").build()
        override fun doInBackground(vararg p0: Void?): List<RestaurantDetailEntity> {
           return db.restaurantDetailDao().getAllDetail()
        }

    }
}