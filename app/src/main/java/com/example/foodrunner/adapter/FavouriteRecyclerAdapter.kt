package com.example.foodrunner.adapter

import android.content.Context
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.foodrunner.R
import com.example.foodrunner.database.RestaurantDatabase
import com.example.foodrunner.database.RestaurantEntity
import com.squareup.picasso.Picasso

class FavouriteRecyclerAdapter(val context:Context,var itemList:List<RestaurantEntity>):RecyclerView.Adapter<FavouriteRecyclerAdapter.FavouriteViewHolder>() {

    class FavouriteViewHolder(view: View):RecyclerView.ViewHolder(view){

        val imgFoodImage=view.findViewById(R.id.imgFoodImage) as ImageView
        val textFoodName=view.findViewById(R.id.textFoodName) as TextView
        val textBookPrice=view.findViewById(R.id.textFoodPrice) as TextView
        val textFoodRating=view.findViewById(R.id.textFoodRating) as TextView
        val layoutContent=view.findViewById(R.id.layoutContent) as LinearLayout
        val favImage=view.findViewById(R.id.checkFavourite) as ImageView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.recycler_home_single_row,parent,false)
        return FavouriteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val restaurant=itemList[position]
        holder.textFoodName.text=restaurant.name
        holder.textBookPrice.text= restaurant.costForOne+"/person"
        holder.textFoodRating.text=restaurant.rating
        holder.favImage.setImageResource(R.drawable.ic_favourites_filled)
        Picasso.get().load(restaurant.imageUrl).error(R.drawable.ic_food).into(holder.imgFoodImage)
        holder.favImage.setOnClickListener{
            val restaurantEntity = RestaurantEntity(
                restaurant.id,
                restaurant.name,
                restaurant.rating,
                restaurant.costForOne,
                restaurant.imageUrl
            )
            var result=RecyclerAsyncTask(context,restaurantEntity).execute().get()
            if(result)
            {
                Toast.makeText(context,"Removed from favourites",Toast.LENGTH_SHORT).show()
                notifyItemRemoved(position)
                notifyItemRangeRemoved(position,1)
                holder.itemView.visibility=View.GONE
            }
        }
    }
    class RecyclerAsyncTask(val context:Context,val restaurantEntity: RestaurantEntity): AsyncTask<Void,Void,Boolean>()
    {
        var db= Room.databaseBuilder(context,RestaurantDatabase::class.java,"res-db").build()
        override fun doInBackground(vararg p0: Void?): Boolean {
             db.restaurantDao().deleteRestaurant(restaurantEntity)
             return true
        }

    }

}