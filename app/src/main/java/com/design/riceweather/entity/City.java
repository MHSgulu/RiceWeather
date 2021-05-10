package com.design.riceweather.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity()
public class City {
    @PrimaryKey(autoGenerate = true)
    public int cid;

    @ColumnInfo(name = "city_name")
    public String cityName;

    public City(String cityName) {
        this.cityName = cityName;
    }
}

