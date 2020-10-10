package com.example.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.R
import com.example.foodrunner.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class PasswordOTP : AppCompatActivity() {

    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var otp:EditText
    lateinit var new_password:EditText
    lateinit var confirm_new_password:EditText
    lateinit var button_next_confirm:Button
    lateinit var sharedPreferences:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_o_t_p)
        toolbar=findViewById(R.id.toolbar)
        setUpToolBar()
        otp=findViewById(R.id.otp)
        new_password=findViewById(R.id.new_password)
        confirm_new_password=findViewById(R.id.confirm_new_password)
        button_next_confirm=findViewById(R.id.button_next_confirm)
        sharedPreferences=getSharedPreferences(getString(R.string.shared_preferences),Context.MODE_PRIVATE)
        button_next_confirm.setOnClickListener{
            if(TextUtils.isEmpty(otp.text))
            {
                otp.error="OTP is required"
            }
            else
            {
                if(TextUtils.isEmpty(new_password.text)|| new_password.text.length<5)
                {
                    new_password.error="Invalid Password"
                }
                else
                {
                    if(new_password.text.toString()==confirm_new_password.text.toString())
                    {
                        if(ConnectionManager().checkConnectivity(this@PasswordOTP))
                        {
                            try {
                                val loginUser=JSONObject()
                                loginUser.put("mobile_number",intent.getStringExtra("verify_phone"))
                                loginUser.put("password",new_password.text.toString())
                                loginUser.put("otp",otp.text.toString())
                                val queue= Volley.newRequestQueue(this@PasswordOTP)
                                val url="http://13.235.250.119/v2/reset_password/fetch_result"
                                val jsonObjectRequest=object:JsonObjectRequest(Method.POST,url,loginUser,com.android.volley.Response.Listener {
                                    println("OTPResponse${it}")
                                    val response=it.getJSONObject("data")
                                    val success=response.getBoolean("success")
                                    if(success)
                                    {
                                        val serverMessage=response.getString("successMessage")
                                        Toast.makeText(this@PasswordOTP,serverMessage,Toast.LENGTH_SHORT).show()
                                        val intent=Intent(this@PasswordOTP,Login::class.java)
                                        sharedPreferences.edit().clear().apply()
                                        startActivity(intent)
                                    }
                                    else
                                    {
                                        val responseMessageServer=response.getString("errorMessage")
                                        Toast.makeText(this@PasswordOTP,responseMessageServer.toString(),Toast.LENGTH_SHORT).show()
                                    }

                                },com.android.volley.Response.ErrorListener {
                                    Toast.makeText(this@PasswordOTP,"Some error occurred",Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@PasswordOTP,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
                            }
                        }
                        else
                        {
                            val dialog=AlertDialog.Builder(this@PasswordOTP)
                            dialog.setTitle("Error")
                            dialog.setMessage("Internet connection is not found")
                            dialog.setPositiveButton("Open Settings"){text,listener->
                                val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                                startActivity(settingsIntent)
                                finish()
                            }
                            dialog.setNegativeButton("Exit"){text,listener->
                                ActivityCompat.finishAffinity(this@PasswordOTP)
                            }
                            dialog.create()
                            dialog.show()
                        }
                    }
                    else
                    {
                        confirm_new_password.error="Passwords don't match"
                    }
                }
            }
        }
    }
    fun setUpToolBar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.title="Verify OTP"
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