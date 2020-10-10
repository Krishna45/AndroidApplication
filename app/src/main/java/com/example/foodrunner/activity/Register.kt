package com.example.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.R
import com.example.foodrunner.fragment.Home
import com.example.foodrunner.util.ConnectionManager
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject

class Register:AppCompatActivity() {

    lateinit var name:EditText
    lateinit var email:EditText
    lateinit var phone:EditText
    lateinit var address:EditText
    lateinit var password:EditText
    lateinit var confirm_password:EditText
    lateinit var registerButton:Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        sharedPreferences=getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        val isLoggedIn=sharedPreferences.getBoolean("isLoggedIn",false)
        name=findViewById(R.id.name)
        email=findViewById(R.id.email)
        phone=findViewById(R.id.phone)
        address=findViewById(R.id.address)
        password=findViewById(R.id.password)
        confirm_password=findViewById(R.id.password_confirm)
        registerButton=findViewById(R.id.register_btn)
        toolbar=findViewById(R.id.toolbar)
        setUpToolBar()
        registerButton.setOnClickListener {
            var u_name = name.text.toString()
            var u_email = email.text.toString()
            var u_phone = phone.text.toString()
            var u_address = address.text.toString()
            var u_password = password.text.toString()
            var u_confirm_password = confirm_password.text.toString()
            if(TextUtils.isEmpty(u_name))
            {
                Toast.makeText(this@Register,"Enter your name",Toast.LENGTH_SHORT).show()
            }
            else if(TextUtils.isEmpty(u_email)) {Toast.makeText(this@Register,"Enter your email",Toast.LENGTH_SHORT).show()}
            else if(TextUtils.isEmpty(u_phone)){Toast.makeText(this@Register,"Enter your phone",Toast.LENGTH_SHORT).show()}
            else if(u_phone.length<10){Toast.makeText(this@Register,"Phone Number should be of 10 digits",Toast.LENGTH_SHORT).show()}
            else if(TextUtils.isEmpty(u_address)){Toast.makeText(this@Register,"Enter your address",Toast.LENGTH_SHORT).show()}
            else if(TextUtils.isEmpty(u_password)){Toast.makeText(this@Register,"Enter your password",Toast.LENGTH_SHORT).show()}
            else if(u_password.length<5){Toast.makeText(this@Register,"Password should be minimum 5 characters",Toast.LENGTH_SHORT).show()}
            else if(TextUtils.isEmpty(u_confirm_password)){Toast.makeText(this@Register,"Confirm your password",Toast.LENGTH_SHORT).show()}
            else if(!u_password.equals(u_confirm_password)){Toast.makeText(this@Register,"Passwords don't match",Toast.LENGTH_SHORT).show()}
            else {
                if(ConnectionManager().checkConnectivity(this))
                {
                    try{
                        val registerUser=JSONObject()
                        registerUser.put("name",u_name)
                        registerUser.put("mobile_number",u_phone)
                        registerUser.put("password",u_password)
                        registerUser.put("address",u_address)
                        registerUser.put("email",u_email)
                        val queue=Volley.newRequestQueue(this)
                        val url="http://13.235.250.119/v2/register/fetch_result"
                        val jsonObjectRequest=object:JsonObjectRequest(Method.POST,url,registerUser,com.android.volley.Response.Listener {

                            val response=it.getJSONObject("data")
                            val success=response.getBoolean("success")
                            if(success)
                            {
                                val data=response.getJSONObject("data")
                                sharedPreferences.edit().putBoolean("isLoggedIn",true).apply()
                                sharedPreferences.edit().putString("user_id",data.getString("user_id")).apply()
                                sharedPreferences.edit().putString("name",data.getString("name")).apply()
                                sharedPreferences.edit().putString("email",data.getString("email")).apply()
                                sharedPreferences.edit().putString("phone",data.getString("mobile_number")).apply()
                                sharedPreferences.edit().putString("address",data.getString("address")).apply()
                                Toast.makeText(this@Register,"Registered Successfully",Toast.LENGTH_SHORT).show()
                                val intent= Intent(this@Register,MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else
                            {
                                val responseMessageServer=response.getString("errorMessage")
                                Toast.makeText(this@Register,responseMessageServer.toString(),Toast.LENGTH_SHORT).show()
                            }
                        },com.android.volley.Response.ErrorListener {
                                 Toast.makeText(this@Register,"Some error occurred",Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@Register,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()

                    }
                }
                else
                {
                    val dialog=AlertDialog.Builder(this@Register)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet connection is not found")
                    dialog.setPositiveButton("Open Settings"){text,listener->
                        val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        finish()
                    }
                    dialog.setNegativeButton("Exit"){text,listener->
                        ActivityCompat.finishAffinity(this@Register)
                    }
                    dialog.create()
                    dialog.show()
                }


            }
        }
    }
    /*fun savePreference(u_name:String,u_email:String,u_phone:String,u_address:String,u_password:String,u_confirm_password:String)
    {
        sharedPreferences.edit().putBoolean("isLoggedIn",true).apply()
        sharedPreferences.edit().putString("name",u_name).apply()
        sharedPreferences.edit().putString("email",u_email).apply()
        sharedPreferences.edit().putString("phone",u_phone).apply()
        sharedPreferences.edit().putString("address",u_address).apply()
    }*/
    fun setUpToolBar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.title="Register Yourself"
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