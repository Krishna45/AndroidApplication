package com.example.foodrunner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RestaurantDetailDao {

    @Insert
    fun insertDetail(restaurantDetailEntity: RestaurantDetailEntity)

    @Delete
    fun deleteDetail(restaurantDetailEntity: RestaurantDetailEntity)

    @Query("DELETE FROM detail")
    fun deleteAll()

    @Query("SELECT * FROM detail")
    fun getAllDetail():List<RestaurantDetailEntity>

    @Query("SELECT * FROM detail WHERE id= :foodId AND restaurantId= :resId")
    fun getFoodItemById(foodId:String,resId:String):RestaurantDetailEntity

}