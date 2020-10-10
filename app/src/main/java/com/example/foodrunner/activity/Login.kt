package com.example.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.R
import com.example.foodrunner.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class Login:AppCompatActivity() {

    lateinit var register_btn: TextView
    lateinit var change_password: TextView
    lateinit var btnLogin:Button
    lateinit var phone:EditText
    lateinit var password:EditText
    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        register_btn=findViewById(R.id.register_btn);
        change_password=findViewById(R.id.change_password);
        toolbar=findViewById(R.id.toolbar)
        btnLogin=findViewById(R.id.login_btn)
        phone=findViewById(R.id.phone)
        password=findViewById(R.id.password)
        setUpToolBar()
        register_btn.setOnClickListener {
            var intent = Intent(this@Login, Register::class.java);
            startActivity(intent)
        }
        change_password.setOnClickListener{
            var intent= Intent(this@Login,
                PasswordChange::class.java)
            startActivity(intent)
        }
        btnLogin.setOnClickListener{
            if(TextUtils.isEmpty(phone.text.toString())||phone.text.length!=10)
            {
                phone.error="Enter valid Mobile Number"
            }
            else
            {
                if(TextUtils.isEmpty(password.text.toString())||password.text.length<5) {
                    password.error = "Enter valid Password"
                }
                else {
                    userLogin()
                }
            }
        }
    }
    fun setUpToolBar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.title="Login"
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
    fun userLogin()
    {
        val sharedPreferences=getSharedPreferences(getString(R.string.shared_preferences),Context.MODE_PRIVATE)
        if(ConnectionManager().checkConnectivity(this@Login))
        {
            try {
                val loginUser=JSONObject()
                loginUser.put("mobile_number",phone.text.toString())
                loginUser.put("password",password.text.toString())
                val queue= Volley.newRequestQueue(this)
                val url="http://13.235.250.119/v2/login/fetch_result/"
                val jsonObjectRequest=object:JsonObjectRequest(Method.POST,url,loginUser,com.android.volley.Response.Listener{

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
                        Toast.makeText(this@Login,"Welcome ${data.getString("name")}",Toast.LENGTH_SHORT).show()
                        val intent= Intent(this@Login,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else
                    {
                        val responseMessageServer=response.getString("errorMessage")
                        Toast.makeText(this@Login,responseMessageServer.toString(),Toast.LENGTH_SHORT).show()
                    }

                },com.android.volley.Response.ErrorListener {
                       Toast.makeText(this@Login,"Some error occurred",Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@Login,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            val dialog=AlertDialog.Builder(this@Login)
            dialog.setTitle("Error")
            dialog.setMessage("Internet connection is not found")
            dialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit"){text,listener->
                ActivityCompat.finishAffinity(this@Login)
            }
            dialog.create()
            dialog.show()
        }
    }
}