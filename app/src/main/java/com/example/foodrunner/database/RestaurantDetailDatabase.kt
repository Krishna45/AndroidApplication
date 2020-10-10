package com.example.foodrunner.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RestaurantDetailEntity::class],version = 1)
abstract class RestaurantDetailDatabase:RoomDatabase() {

    abstract fun restaurantDetailDao():RestaurantDetailDao

}