package com.design.riceweather.databse;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.design.riceweather.databse.dao.CityDao;
import com.design.riceweather.entity.City;

@Database(entities = {City.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CityDao cityDao();

}

