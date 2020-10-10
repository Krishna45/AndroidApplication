package com.example.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrunner.R
import com.example.foodrunner.model.CartItems
import kotlinx.android.synthetic.main.order_single_row.view.*

class CartRecyclerAdapter(val context: Context,val itemList:ArrayList<CartItems>):RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {

       class CartViewHolder(view:View):RecyclerView.ViewHolder(view)
       {
             val foodName=view.findViewById(R.id.foodName) as TextView
             val foodPrice=view.findViewById(R.id.foodPrice) as TextView
       }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.order_single_row,parent,false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem=itemList[position]
        holder.foodName.text=cartItem.itemName
        holder.foodPrice.text="Rs."+cartItem.itemPrice
    }

}