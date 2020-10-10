package com.example.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.foodrunner.R
import com.example.foodrunner.fragment.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.register.view.*

class MainActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var name:TextView
    lateinit var phone:TextView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var navView:View
    var previousMenuItem:MenuItem?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences=getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        drawerLayout=findViewById(R.id.drawerLayout)
        coordinatorLayout=findViewById(R.id.coordinatorLayout)
        toolbar=findViewById(R.id.toolbar)
        frameLayout=findViewById(R.id.frameLayout)
        navigationView=findViewById(R.id.navigationView)
        navView=navigationView.inflateHeaderView(R.layout.drawer_header)
        name=navView.findViewById(R.id.displayName)
        phone=navView.findViewById(R.id.displayPhone)
        setUpToolBar()
        openHomePage()
        if(sharedPreferences.getBoolean("isLoggedIn",false))
        {
            name.text=sharedPreferences.getString("name","FoodRunner")
            phone.text="+91-"+sharedPreferences.getString("phone","Phone")
        }
        val actionBarDrawerToggle=ActionBarDrawerToggle(this@MainActivity,drawerLayout,R.string.open_drawer,R.string.close_drawer)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            if(previousMenuItem!=null)
            {
                previousMenuItem?.isChecked=false
            }
            it.isCheckable=true
            it.isChecked=true
            when(it.itemId){
               R.id.homePage->{
                   openHomePage()
                   drawerLayout.closeDrawers()
               }
                R.id.profile->{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, Profile())
                        .commit()
                    supportActionBar?.title="My Profile"
                    drawerLayout.closeDrawers()
                }
                R.id.favourite->{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout,Favourites())
                        .commit()
                    supportActionBar?.title="Favourites"
                    drawerLayout.closeDrawers()
                }
                R.id.help->{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout,FAQScreen())
                        .commit()
                    supportActionBar?.title="Frequently Asked Questions"
                    drawerLayout.closeDrawers()
                }
                R.id.orderHistory->{
                    val intent=Intent(this,OrderHistory::class.java)
                    drawerLayout.closeDrawers()
                    startActivity(intent)
                }
                R.id.logout->{
                    var dialog=AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Confirmation")
                    dialog.setMessage("Are you sure you want to logout?")
                    dialog.setPositiveButton("YES"){text,listener->
                        sharedPreferences.edit().clear().apply()
                        val intent=Intent(this@MainActivity,Login::class.java)
                        startActivity(intent)
                        finish()
                    }
                    dialog.setNegativeButton("NO"){text,listener->
                      drawerLayout.closeDrawers()
                    }
                    dialog.create()
                    dialog.show()

                }
            }
            return@setNavigationItemSelectedListener true
        }
    }
    fun setUpToolBar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.title="Home"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        if(id==android.R.id.home)
        {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }
    fun openHomePage()
    {
        val fragment= Home()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout,fragment)
        transaction.commit()
        supportActionBar?.title="All Restaurants"
        navigationView.setCheckedItem(R.id.homePage)
    }

    override fun onBackPressed() {
        val frag=supportFragmentManager.findFragmentById(R.id.frameLayout)
        when(frag)
        {
            !is Home->openHomePage()
            else-> super.onBackPressed()
        }
    }
}