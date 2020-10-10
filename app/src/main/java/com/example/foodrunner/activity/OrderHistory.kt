package com.example.foodrunner.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.R
import com.example.foodrunner.adapter.CartRecyclerAdapter
import com.example.foodrunner.adapter.OrderRecyclerAdapter
import com.example.foodrunner.fragment.Home
import com.example.foodrunner.model.Order
import com.example.foodrunner.util.ConnectionManager
import org.json.JSONException
import java.lang.Exception

class OrderHistory : AppCompatActivity() {

    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var recyclerItem:RecyclerView
    lateinit var progressLayout:RelativeLayout
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter:OrderRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)
        toolbar=findViewById(R.id.toolbar)
        recyclerItem=findViewById(R.id.recyclerItem)
        progressLayout=findViewById(R.id.progressLayout)
        progressLayout.visibility=View.GONE
        setUpToolBar()
        layoutManager=LinearLayoutManager(this@OrderHistory)
        val orderedRestaurantList=ArrayList<Order>()
        val sharedPreferences=getSharedPreferences(getString(R.string.shared_preferences),Context.MODE_PRIVATE)
        val userId=sharedPreferences.getString("user_id","Id")
        if(ConnectionManager().checkConnectivity(this@OrderHistory))
        {
            try{

                val queue=Volley.newRequestQueue(this@OrderHistory)
                val url="http://13.235.250.119/v2/orders/fetch_result/${userId}"
                val jsonObjectRequest=object:JsonObjectRequest(Method.GET,url,null,com.android.volley.Response.Listener {

                    val response = it.getJSONObject("data")
                    val success = response.getBoolean("success")
                    if (success) {
                        val data = response.getJSONArray("data")
                        if (data.length() == 0) {
                            progressLayout.visibility = View.VISIBLE
                        } else {
                            progressLayout.visibility = View.GONE
                            for (i in 0 until data.length()) {
                                val restaurantItem = data.getJSONObject(i)
                                val restaurantObject = Order(
                                    restaurantItem.getString("order_id"),
                                    restaurantItem.getString("restaurant_name"),
                                    restaurantItem.getString("total_cost"),
                                    restaurantItem.getString("order_placed_at").substring(0, 10)
                                )
                                orderedRestaurantList.add(restaurantObject)
                                recyclerAdapter =
                                    OrderRecyclerAdapter(this@OrderHistory, orderedRestaurantList)
                                recyclerItem.adapter = recyclerAdapter
                                recyclerItem.layoutManager = layoutManager
                                recyclerItem.itemAnimator = DefaultItemAnimator()
                                recyclerItem.setHasFixedSize(true)
                            }
                        }
                    } else {
                        Toast.makeText(this@OrderHistory, "Some error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }



                },com.android.volley.Response.ErrorListener {
                    Toast.makeText(this@OrderHistory,"Some error occurred",Toast.LENGTH_SHORT).show()
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
            catch (e:JSONException)
            {
                Toast.makeText(this@OrderHistory,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            val dialog=AlertDialog.Builder(this@OrderHistory)
            dialog.setTitle("Error")
            dialog.setMessage("Internet connection is not found")
            dialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit"){text,listener->
                ActivityCompat.finishAffinity(this@OrderHistory)
            }
            dialog.create()
            dialog.show()
        }
    }
    fun setUpToolBar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.title="My Previous Orders"
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

}