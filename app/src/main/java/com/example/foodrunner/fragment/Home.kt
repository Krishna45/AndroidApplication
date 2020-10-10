package com.example.foodrunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.R
import com.example.foodrunner.adapter.HomeRecyclerAdapter
import com.example.foodrunner.model.Restaurant
import com.example.foodrunner.util.ConnectionManager
import kotlinx.android.synthetic.main.sort_button.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Home : Fragment() {

    lateinit var recyclerHome:RecyclerView
    lateinit var layoutManager:RecyclerView.LayoutManager
    lateinit var recyclerAdapter: HomeRecyclerAdapter
    lateinit var progressBar:ProgressBar
    lateinit var progressLayout:RelativeLayout
    lateinit var radioButtonView:View
    lateinit var find:RelativeLayout
    lateinit var textSearch:EditText
    var restaurantListInfo=ArrayList<Restaurant>()

    var ratingComparator= Comparator<Restaurant>{
        res1,res2->
        if(res1.rating.compareTo(res2.rating,true)==0){
            res1.name.compareTo(res2.name,true)
        }
        else
        {
            res1.rating.compareTo(res2.rating,true)
        }

    }
    var costComparator= Comparator<Restaurant>{
        res1,res2->
        res1.costForOne.compareTo(res2.costForOne,true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view= inflater.inflate(R.layout.fragment_home, container, false)
        recyclerHome=view.findViewById(R.id.recyclerHome)
        progressLayout=view.findViewById(R.id.progressLayout)
        progressBar=view.findViewById(R.id.progressBar)
        progressLayout.visibility=View.VISIBLE
        layoutManager=LinearLayoutManager(activity)
        find=view.findViewById(R.id.find)
        textSearch=view.findViewById(R.id.textSearch)
        find.visibility=View.GONE
        val queue= Volley.newRequestQueue(activity as Context)
        val url="http://13.235.250.119/v2/restaurants/fetch_result/"
        if(ConnectionManager().checkConnectivity(activity as Context))
        {
           val jsonObjectTequest=object: JsonObjectRequest(Request.Method.GET,url,null, Response.Listener {
                  println("Response is $it")
                    try{
                        val response=it.getJSONObject("data")
                        val success=response.getBoolean("success")
                        println("success"+success)
                        if(success){
                           progressLayout.visibility=View.GONE
                           val data = response.getJSONArray("data")
                           for (i in 0 until data.length()) {
                               val resJSONObject = data.getJSONObject(i)
                               val resObject = Restaurant(
                                   resJSONObject.getString("id"),
                                   resJSONObject.getString("name"),
                                   resJSONObject.getString("rating"),
                                   resJSONObject.getString("cost_for_one"),
                                   resJSONObject.getString("image_url")
                               )

                               restaurantListInfo.add(resObject)
                               if (activity != null) {
                                   recyclerAdapter =
                                       HomeRecyclerAdapter(activity as Context, restaurantListInfo)
                                   recyclerHome.adapter = recyclerAdapter
                                   recyclerHome.layoutManager = layoutManager
                                   recyclerHome.itemAnimator=DefaultItemAnimator()
                                   recyclerHome.setHasFixedSize(true)
                               }

                           }
                       }
                   else
                   {
                       Toast.makeText(activity as Context,"Some error occurred",Toast.LENGTH_SHORT).show()
                   }
               }
               catch (e:JSONException){
                     println("Actual"+e.printStackTrace())
                     Toast.makeText(activity as Context,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
               }
           },Response.ErrorListener {
               println("Error is $it")
                  if(activity!=null)
                  {
                      Toast.makeText(activity as Context,"Some error occurred",Toast.LENGTH_SHORT).show()
                  }
           }){
               override fun getHeaders(): MutableMap<String, String> {
                   val headers=HashMap<String,String>()
                   headers["Content-type"]="application/json"
                   headers["token"]="6061989b41e55e"
                   return headers
               }
           }
            queue.add(jsonObjectTequest)
        }
        else
        {
            val dialog=AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet connection is not found")
            dialog.setPositiveButton("Open Settings"){text,listener->
                   val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                   startActivity(settingsIntent)
                   activity?.finish()
            }
            dialog.setNegativeButton("Exit"){text,listener->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        fun filterFun(str:String){
            val filteredList= arrayListOf<Restaurant>()
            for(i in restaurantListInfo)
            {
                if(i.name.toLowerCase(Locale.ROOT).contains(str.toLowerCase(Locale.ROOT)))
                {
                    filteredList.add(i)
                }
            }
            if(filteredList.size==0){
                find.visibility=View.VISIBLE
            }
            else{
                find.visibility=View.GONE
            }
            recyclerAdapter.filterList(filteredList)
        }
        textSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(str: Editable?) {
                filterFun(str.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> {
                radioButtonView = View.inflate(context, R.layout.sort_button, null)
                androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                    .setTitle("Sort By")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") { text, listener ->
                        if (radioButtonView.radio_high_to_low.isChecked) {
                            Collections.sort(restaurantListInfo, costComparator)
                            restaurantListInfo.reverse()
                            recyclerAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_low_to_high.isChecked) {
                            Collections.sort(restaurantListInfo, costComparator)
                            recyclerAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_rating.isChecked) {
                            Collections.sort(restaurantListInfo, ratingComparator)
                            restaurantListInfo.reverse()
                            recyclerAdapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton("Cancel") { text, listener ->

                    }
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

