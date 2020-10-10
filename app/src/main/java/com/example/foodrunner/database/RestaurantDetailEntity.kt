package com.example.foodrunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detail",primaryKeys = ["id","restaurantId"])
data class RestaurantDetailEntity(
    var id:String,
    @ColumnInfo(name="name") var name:String,
    @ColumnInfo(name="costForOne") var costForOne:String,
    var restaurantId:String

)
