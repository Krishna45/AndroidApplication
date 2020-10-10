package com.example.foodrunner.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Parcel
import android.os.Parcelable
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.foodrunner.R
import com.example.foodrunner.activity.RestaurantDetails
import com.example.foodrunner.database.RestaurantDatabase
import com.example.foodrunner.database.RestaurantEntity
import com.example.foodrunner.model.Restaurant
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.recycler_home_single_row.view.*

class HomeRecyclerAdapter(val context:Context,var itemList:ArrayList<Restaurant>): RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>(){

    var sharedPreference=context.getSharedPreferences("Preferences",Context.MODE_PRIVATE)
    class HomeViewHolder(view:View): RecyclerView.ViewHolder(view)
    {
         val imgFoodImage=view.findViewById(R.id.imgFoodImage) as ImageView
         val textFoodName=view.findViewById(R.id.textFoodName) as TextView
         val textFoodPrice=view.findViewById(R.id.textFoodPrice) as TextView
         val textFoodRating=view.findViewById(R.id.textFoodRating) as TextView
         val layoutContent=view.findViewById(R.id.layoutContent) as LinearLayout
         val favImage=view.findViewById(R.id.checkFavourite) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.recycler_home_single_row,parent,false)
        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
       return itemList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        val restaurant=itemList[position]
        holder.textFoodName.text=restaurant.name
        holder.textFoodPrice.text= restaurant.costForOne+"/person"
        holder.textFoodRating.text=restaurant.rating
        Picasso.get().load(restaurant.imageUrl).error(R.drawable.ic_food).into(holder.imgFoodImage)
        val listOfFavourites = GetAllFavAsyncTask(context).execute().get()
        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(restaurant.id)) {
            holder.favImage.setImageResource(R.drawable.ic_favourites_filled)
        } else {
            holder.favImage.setImageResource(R.drawable.ic_favourites_line)
        }

        holder.favImage.setOnClickListener {
            val restaurantEntity = RestaurantEntity(
                restaurant.id,
                restaurant.name,
                restaurant.rating,
                restaurant.costForOne,
                restaurant.imageUrl
            )

            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val async =
                    DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    holder.favImage.setImageResource(R.drawable.ic_favourites_filled)
                    Toast.makeText(context,"Added to favourites",Toast.LENGTH_SHORT).show()
                }
            } else {
                val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                val result = async.get()

                if (result) {
                    holder.favImage.setImageResource(R.drawable.ic_favourites_line)
                    Toast.makeText(context,"Removed from favourites",Toast.LENGTH_SHORT).show()
                }
            }
        }

        holder.layoutContent.setOnClickListener {
            val intent= Intent(context,RestaurantDetails::class.java)
            sharedPreference.edit().putString("restaurant_id",restaurant.id).apply()
            sharedPreference.edit().putString("restaurant_name",restaurant.name).apply()
            intent.putExtra("restaurant_id",restaurant.id)
            intent.putExtra("restaurant_name",restaurant.name)
            context.startActivity(intent)
        }
    }
    class DBAsyncTask(context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            /*
            Mode 1 -> Check DB if the Restaurant is favourite or not
            Mode 2 -> Save the Restaurant into DB as favourite
            Mode 3 -> Remove the favourite Restaurant
            */

            when (mode) {

                1 -> {
                    val res: RestaurantEntity? =
                        db.restaurantDao().getRestaurantById(restaurantEntity.id.toString())
                    db.close()
                    return res != null
                }

                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }

            return false
        }

    }




    class GetAllFavAsyncTask(
        context: Context
    ) :
        AsyncTask<Void, Void, ArrayList<String>>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): ArrayList<String> {

            val list = db.restaurantDao().getAllRestaurant()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.id.toString())
            }
            return listOfIds
        }
    }
    fun filterList(filteredList:ArrayList<Restaurant>)
    {
        itemList=filteredList
        notifyDataSetChanged()
    }

}











