package com.example.happyplaces.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places-table")
data class PlaceEntity(

    // Mandatory Fields
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,

    // Temporary Fields
    val category: String = "",
    val streetAddress1: String = "",
    val streetAddress2: String = "",
//    val phoneNumber: String = "",
//    val emailAddress: String = "",
//    val website: String = "",
    val city: String = "",
    val state: String = "",
    val zip: String = "",
    val rating: Float = 0.0f,
    val isFavorite: Int = 0
)
