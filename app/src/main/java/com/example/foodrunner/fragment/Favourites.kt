package com.example.foodrunner.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import com.example.foodrunner.R
import com.example.foodrunner.adapter.FavouriteRecyclerAdapter
import com.example.foodrunner.adapter.HomeRecyclerAdapter
import com.example.foodrunner.database.RestaurantDatabase
import com.example.foodrunner.database.RestaurantEntity
import com.example.foodrunner.model.Restaurant


class Favourites : Fragment() {

    private lateinit var recyclerRestaurant: RecyclerView
    private lateinit var allRestaurantsAdapter: FavouriteRecyclerAdapter
    private var restaurantList =listOf<Restaurant>()
    private lateinit var progressLayout:RelativeLayout
    lateinit var layoutManager:RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_favourites, container, false)
        recyclerRestaurant=view.findViewById(R.id.recyclerRestaurant)
        progressLayout=view.findViewById(R.id.progressLayout)
        val itemList=RetrieveAsyncTask(activity as Context).execute().get()
        if(itemList.isEmpty()){
            progressLayout.visibility=View.VISIBLE
        }
        else {
            progressLayout.visibility=View.GONE
            allRestaurantsAdapter = FavouriteRecyclerAdapter(activity as Context, itemList)
            layoutManager = LinearLayoutManager(activity)
            recyclerRestaurant.layoutManager = layoutManager
            recyclerRestaurant.itemAnimator = DefaultItemAnimator()
            recyclerRestaurant.adapter = allRestaurantsAdapter
            recyclerRestaurant.setHasFixedSize(true)
        }
        return view
    }
    class RetrieveAsyncTask(val context: Context):AsyncTask<Void,Void,List<RestaurantEntity>>()
    {
        var db= Room.databaseBuilder(context,RestaurantDatabase::class.java,"res-db").build()
        override fun doInBackground(vararg p0: Void?): List<RestaurantEntity>{
            return db.restaurantDao().getAllRestaurant()
        }
    }

}