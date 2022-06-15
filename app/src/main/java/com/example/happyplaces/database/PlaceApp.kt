package com.example.happyplaces.database

import android.app.Application

class PlaceApp: Application() {
    val db by lazy {
        PlaceDatabase.getInstance(this)
    }
}