package com.example.foodrunner.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.foodrunner.R
import org.w3c.dom.Text


class Profile : Fragment() {

    lateinit var name:TextView
    lateinit var phone:TextView
    lateinit var email:TextView
    lateinit var address:TextView
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_profile, container, false)
        sharedPreferences=
            activity?.getSharedPreferences(getString(R.string.shared_preferences),Context.MODE_PRIVATE)!!
        name=view.findViewById(R.id.name)
        phone=view.findViewById(R.id.phone)
        email=view.findViewById(R.id.email)
        address=view.findViewById(R.id.address)
        if(sharedPreferences.getBoolean("isLoggedIn",false))
        {
            name.text=sharedPreferences.getString("name","Name")
            phone.text=sharedPreferences.getString("phone","Phone")
            email.text=sharedPreferences.getString("email","Email")
            println("Address ${sharedPreferences.getString("address","Address")}")
            address.text=sharedPreferences.getString("address","Address")
        }
        return view
    }


}