package com.example.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.R
import com.example.foodrunner.model.CartItems
import com.example.foodrunner.model.Order
import com.example.foodrunner.util.ConnectionManager
import kotlinx.android.synthetic.main.order_history_single_row.view.*
import org.json.JSONException

class OrderRecyclerAdapter(val context: Context,val itemList:ArrayList<Order>):RecyclerView.Adapter<OrderRecyclerAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        val textRestaurantName=view.findViewById(R.id.textRestaurantName) as TextView
        val textDate=view.findViewById(R.id.textDate) as TextView
        val recyclerViewForItemsOrdered=view.findViewById(R.id.recyclerViewForItemsOrdered) as RecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.order_history_single_row,parent,false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val restaurantObject=itemList[position]
        holder.textRestaurantName.text=restaurantObject.restaurantName
        var formatDate=restaurantObject.orderPlacedAt
        formatDate=formatDate.replace("-","/")
        formatDate=formatDate.substring(0,6)+"20"+formatDate.substring(6,8)
        holder.textDate.text=formatDate
        val layoutManager=LinearLayoutManager(context)
        var orderedItemAdapter:CartRecyclerAdapter
        if(ConnectionManager().checkConnectivity(context))
        {
           try {
               val orderedItemsPerRestaurant=ArrayList<CartItems>()
               val sharedPreferences=context.getSharedPreferences(context.getString(R.string.shared_preferences),Context.MODE_PRIVATE)
               val userId=sharedPreferences.getString("user_id","Id")
               val queue= Volley.newRequestQueue(context)
               val url="http://13.235.250.119/v2/orders/fetch_result/${userId}"
               val jsonObjectRequest=object:JsonObjectRequest(Method.GET,url,null,com.android.volley.Response.Listener {
                   println("Orders ${it}")
                   val response=it.getJSONObject("data")
                   val success=response.getBoolean("success")
                   if(success)
                   {
                       val data=response.getJSONArray("data")
                       val restaurantObjectRetrieved=data.getJSONObject(position)
                       orderedItemsPerRestaurant.clear()
                       val foodOrdered=restaurantObjectRetrieved.getJSONArray("food_items")
                       for(j in 0 until foodOrdered.length())
                       {
                           val eachFoodItem=foodOrdered.getJSONObject(j)
                           val itemObject=CartItems(
                               eachFoodItem.getString("food_item_id"),
                               eachFoodItem.getString("name"),
                               eachFoodItem.getString("cost"),
                               "Id"
                           )
                           orderedItemsPerRestaurant.add(itemObject)
                       }
                       orderedItemAdapter= CartRecyclerAdapter(context,orderedItemsPerRestaurant)
                       holder.recyclerViewForItemsOrdered.adapter=orderedItemAdapter
                       holder.recyclerViewForItemsOrdered.layoutManager=layoutManager
                       holder.recyclerViewForItemsOrdered.itemAnimator=DefaultItemAnimator()
                       holder.recyclerViewForItemsOrdered.setHasFixedSize(true)
                   }


               },com.android.volley.Response.ErrorListener {
                   Toast.makeText(context,"Some error occurred",Toast.LENGTH_SHORT).show()
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
               Toast.makeText(context,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
           }
        }


    }
}