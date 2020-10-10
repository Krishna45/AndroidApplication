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
import com.example.foodrunner.util.ConnectionManager
import kotlinx.android.synthetic.main.register.*
import org.json.JSONException
import org.json.JSONObject

class PasswordChange:AppCompatActivity() {

    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var phone: EditText
    lateinit var email:EditText
    lateinit var button_next:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_change);
        toolbar=findViewById(R.id.toolbar)
        phone=findViewById(R.id.phone)
        email=findViewById(R.id.email)
        button_next=findViewById(R.id.button_next)
        setUpToolBar()
        button_next.setOnClickListener{
            if(TextUtils.isEmpty(phone.text)||phone.text.length!=10)
            {
                phone.error="Invalid Mobile Number"
            }
            else
            {
                if(TextUtils.isEmpty(email.text))
                {
                    email.error="Email is required"
                }
                else
                {
                    if(ConnectionManager().checkConnectivity(this@PasswordChange))
                    {
                        try {
                            val loginUser=JSONObject()
                            loginUser.put("mobile_number",phone.text.toString())
                            loginUser.put("email",email.text.toString())
                            val queue= Volley.newRequestQueue(this@PasswordChange)
                            val url="http://13.235.250.119/v2/forgot_password/fetch_result"
                            val jsonObjectRequest=object:JsonObjectRequest(Method.POST,url,loginUser,com.android.volley.Response.Listener {

                                val response=it.getJSONObject("data")
                                val success=response.getBoolean("success")
                                if(success)
                                {
                                    val firstTry=response.getBoolean("first_try")
                                    if(firstTry)
                                    {
                                        Toast.makeText(this@PasswordChange,"OTP Sent",Toast.LENGTH_SHORT).show()
                                        val intent=Intent(this@PasswordChange,PasswordOTP::class.java)
                                        intent.putExtra("verify_phone",phone.text.toString())
                                        startActivity(intent)
                                    }
                                    else
                                    {
                                        Toast.makeText(this@PasswordChange,"OTP sent already",Toast.LENGTH_SHORT).show()
                                        val intent=Intent(this@PasswordChange,PasswordOTP::class.java)
                                        intent.putExtra("verify_phone",phone.text.toString())
                                        startActivity(intent)
                                    }
                                }
                                else
                                {
                                    val responseMessageServer=response.getString("errorMessage")
                                    Toast.makeText(this@PasswordChange,responseMessageServer.toString(),Toast.LENGTH_SHORT).show()
                                }

                            },com.android.volley.Response.ErrorListener {
                                Toast.makeText(this@PasswordChange,"Some error occurred",Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@PasswordChange,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
                        }
                    }
                    else
                    {
                        val dialog=AlertDialog.Builder(this@PasswordChange)
                        dialog.setTitle("Error")
                        dialog.setMessage("Internet connection is not found")
                        dialog.setPositiveButton("Open Settings"){text,listener->
                            val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            startActivity(settingsIntent)
                            finish()
                        }
                        dialog.setNegativeButton("Exit"){text,listener->
                            ActivityCompat.finishAffinity(this@PasswordChange)
                        }
                        dialog.create()
                        dialog.show()
                    }
                }
            }
        }
    }
    fun setUpToolBar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.title="Change Password"
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