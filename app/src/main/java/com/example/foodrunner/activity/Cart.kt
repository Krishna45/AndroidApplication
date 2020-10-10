package com.example.foodrunner.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.R
import com.example.foodrunner.adapter.CartRecyclerAdapter
import com.example.foodrunner.adapter.RestaurantDetailsRecyclerAdapter
import com.example.foodrunner.database.RestaurantDetailDatabase
import com.example.foodrunner.database.RestaurantDetailEntity
import com.example.foodrunner.model.CartItems
import com.example.foodrunner.util.ConnectionManager
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class Cart : AppCompatActivity() {
    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var recyclerItems: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var restaurantAdapter: CartRecyclerAdapter
    lateinit var placeOrder: Button
    lateinit var displayRestaurant:TextView
    lateinit var sharedPreferences: SharedPreferences
    var restaurantId:String?=""
    var restaurantName:String?=""
    var cartList=ArrayList<CartItems>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        toolbar=findViewById(R.id.toolbar)
        sharedPreferences=getSharedPreferences("Preferences",Context.MODE_PRIVATE)
        recyclerItems=findViewById(R.id.recyclerItems)
        placeOrder=findViewById(R.id.placeOrder)
        displayRestaurant=findViewById(R.id.displayRestaurant)
        layoutManager=LinearLayoutManager(this@Cart)
        restaurantId=sharedPreferences.getString("restaurant_id","Id")
        restaurantName=sharedPreferences.getString("restaurant_name","Name")
        displayRestaurant.text=sharedPreferences.getString("restaurant_name","Name")
        setUpToolBar()
        var list= listOf<RestaurantDetailEntity>()
        list=GetAllAsyncTask(this).execute().get()
        var sum=0
        for(i in list)
        {
            sum+=(i.costForOne.toInt())
            var result=CartItems(
                i.name,
                i.name,
                i.costForOne,
                i.restaurantId
            )
            cartList.add(result)
            restaurantAdapter= CartRecyclerAdapter(this,cartList)
            recyclerItems.adapter=restaurantAdapter
            recyclerItems.layoutManager=layoutManager
            recyclerItems.itemAnimator= DefaultItemAnimator()
            recyclerItems.setHasFixedSize(true)
        }
        placeOrder.text="Place Order (Total Rs. ${sum})"
        placeOrder.setOnClickListener{
            val sharedPreferences=this.getSharedPreferences(getString(R.string.shared_preferences),Context.MODE_PRIVATE)
            if(ConnectionManager().checkConnectivity(this))
            {
                try{
                    val foodArray=JSONArray()
                    for(i in list)
                    {
                        val singleItemObject=JSONObject()
                        singleItemObject.put("food_item_id",i.id)
                        foodArray.put(singleItemObject)
                    }
                    val sendOrder=JSONObject()
                    sendOrder.put("user_id",sharedPreferences.getString("user_id","Id"))
                    sendOrder.put("restaurant_id",restaurantId)
                    sendOrder.put("total_cost",sum)
                    sendOrder.put("food",foodArray)
                    val queue= Volley.newRequestQueue(this@Cart)
                    val url="http://13.235.250.119/v2/place_order/fetch_result/"
                    val jsonObjectRequest=object:JsonObjectRequest(Method.POST,url,sendOrder,com.android.volley.Response.Listener {
                        println("PlaceOrder $it")
                        val response=it.getJSONObject("data")
                        val success=response.getBoolean("success")
                        if(success)
                        {
                            Toast.makeText(this@Cart,"Order Placed",Toast.LENGTH_SHORT).show()
                            createNotification()
                            val intent= Intent(this@Cart,OrderPlaced::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }
                        else
                        {
                            val responseMessageServer=response.getString("errorMessage")
                            Toast.makeText(this@Cart,responseMessageServer.toString(),Toast.LENGTH_SHORT).show()
                        }

                    },com.android.volley.Response.ErrorListener {

                        Toast.makeText(this@Cart,"Some error occurred",Toast.LENGTH_SHORT).show()

                    })
                    {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers=HashMap<String,String>()
                            headers["Content-type"]="application/json"
                            headers["token"]="6061989b41e55e"
                            return headers
                        }
                    }
                    queue.add(jsonObjectRequest)
                }
                catch (e:Exception){
                    Toast.makeText(this@Cart,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                val dialog=AlertDialog.Builder(this@Cart)
                dialog.setTitle("Error")
                dialog.setMessage("Internet connection is not found")
                dialog.setPositiveButton("Open Settings"){text,listener->
                    val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit"){text,listener->
                    ActivityCompat.finishAffinity(this@Cart)
                }
                dialog.create()
                dialog.show()
            }
        }
    }
    fun setUpToolBar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.title="My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        if(id==android.R.id.home)
        {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
    class GetAllAsyncTask(val context: Context):AsyncTask<Void,Void,List<RestaurantDetailEntity>>(){

        val db= Room.databaseBuilder(context,RestaurantDetailDatabase::class.java,"detail-db").build()
        override fun doInBackground(vararg p0: Void?): List<RestaurantDetailEntity> {
            return db.restaurantDetailDao().getAllDetail()
        }

    }
    fun createNotification()
    {
        val notificationId=1
        val channelId="personal_notification"
        val notificationBuilder=NotificationCompat.Builder(this,channelId)
        notificationBuilder.setSmallIcon(R.drawable.food_app)
        notificationBuilder.setContentTitle("Order Placed")
        notificationBuilder.setContentText("Your order has been successfully placed")
        notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText("Ordered from ${restaurantName}"))
        notificationBuilder.priority=NotificationCompat.PRIORITY_DEFAULT
        val notificationManagerCompat=NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId,notificationBuilder.build())
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            val name="Order Placed"
            val description="Your order has been successfully placed"
            val importance=NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel=NotificationChannel(channelId,name,importance)
            notificationChannel.description=description
            val notificationManager=(getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}