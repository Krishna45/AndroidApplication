package com.example.foodrunner.adapter

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.foodrunner.R
import com.example.foodrunner.activity.RestaurantDetails
import com.example.foodrunner.database.RestaurantDetailDatabase
import com.example.foodrunner.database.RestaurantDetailEntity
import com.example.foodrunner.database.RestaurantEntity
import com.example.foodrunner.model.Details
import kotlinx.android.synthetic.main.register.*
import kotlinx.android.synthetic.main.restaurant_item_single_row.view.*

class RestaurantDetailsRecyclerAdapter(val context: Context, val itemList:ArrayList<Details>,val addToCart:Button):RecyclerView.Adapter<RestaurantDetailsRecyclerAdapter.DetailsViewHolder>() {

    var list=listOf<RestaurantDetailEntity>()
    lateinit var sharedPreferences:SharedPreferences
    class DetailsViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        val foodItemName=view.findViewById(R.id.foodItemName) as TextView
        val foodItemPrice=view.findViewById(R.id.foodItemPrice) as TextView
        val layoutContent=view.findViewById(R.id.layoutContent) as LinearLayout
        val foodItemAddToCart=view.findViewById(R.id.foodItemAddToCart) as Button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.restaurant_item_single_row,parent,false)
        return DetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
       return itemList.size
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        val restaurant=itemList[position]
        holder.foodItemName.text=restaurant.name
        holder.foodItemPrice.text="Rs. "+restaurant.costForOne
        val restaurantResult=RestaurantDetailEntity(
            restaurant.id,
            restaurant.name,
            restaurant.costForOne,
            restaurant.restaurantId
        )
        val result:Boolean=DBDetailsAsyncTask(context,restaurantResult,1).execute().get()
        if(result)
        {
            holder.foodItemAddToCart.text="Remove"
            val cartColor=ContextCompat.getColor(context,R.color.yellowShade)
            holder.foodItemAddToCart.setBackgroundColor(cartColor)
        }
        else
        {
            holder.foodItemAddToCart.text="Add"
            val noCartColor=ContextCompat.getColor(context,R.color.redShade)
            holder.foodItemAddToCart.setBackgroundColor(noCartColor)
        }
        holder.foodItemAddToCart.setOnClickListener{


              if(!DBDetailsAsyncTask(context,restaurantResult,1).execute().get())
              {
                  val async=DBDetailsAsyncTask(context,restaurantResult,2).execute()
                  val result=async.get()
                  if(result)
                  {
                      Toast.makeText(context,"Added to Cart",Toast.LENGTH_SHORT).show()
                      holder.foodItemAddToCart.text="Remove"
                      val favColor=ContextCompat.getColor(context,R.color.yellowShade)
                      holder.foodItemAddToCart.setBackgroundColor(favColor)
                  }
                  else
                  {
                      Toast.makeText(context,"Some error occurred",Toast.LENGTH_SHORT).show()
                  }
              }
            else
              {
                  val async=DBDetailsAsyncTask(context,restaurantResult,3).execute()
                  val result=async.get()
                  if(result)
                  {
                      Toast.makeText(context,"Removed from Cart",Toast.LENGTH_SHORT).show()
                  }
                  holder.foodItemAddToCart.text="Add"
                  val noFavColor=ContextCompat.getColor(context,R.color.redShade)
                  holder.foodItemAddToCart.setBackgroundColor(noFavColor)
              }
            list=DBGetAllAsyncTask(context).execute().get()
            if(list.size>0)
            {
                addToCart.visibility=View.VISIBLE
            }
            else
            {
                addToCart.visibility=View.GONE
            }
        }
    }
    class DBDetailsAsyncTask(context: Context,val foodEntity: RestaurantDetailEntity,val mode:Int):AsyncTask<Void,Void,Boolean>()
    {
        val db= Room.databaseBuilder(context,RestaurantDetailDatabase::class.java,"detail-db").build()
        override fun doInBackground(vararg p0: Void?): Boolean {
           when(mode)
           {
               1->{
                   val res:RestaurantDetailEntity?=db.restaurantDetailDao().getFoodItemById(foodEntity.id,foodEntity.restaurantId)
                   db.close()
                   return res!=null
               }
               2->{
                   db.restaurantDetailDao().insertDetail(foodEntity)
                   db.close()
                   return true
               }
               3-> {
                   db.restaurantDetailDao().deleteDetail(foodEntity)
                   db.close()
                   return true
               }
           }
            return false
        }

    }
    class DBGetAllAsyncTask(context:Context):AsyncTask<Void,Void,List<RestaurantDetailEntity>>(){
        val db=Room.databaseBuilder(context,RestaurantDetailDatabase::class.java,"detail-db").build()
        override fun doInBackground(vararg p0: Void?): List<RestaurantDetailEntity> {
            return db.restaurantDetailDao().getAllDetail()
        }

    }

}