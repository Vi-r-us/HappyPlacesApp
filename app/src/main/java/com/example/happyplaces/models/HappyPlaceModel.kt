package com.example.happyplaces.models

data class HappyPlaceModel(

    // Mandatory Fields
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,

    // Temporary Fields
    val category: String,
    val streetAddress1: String,
    val streetAddress2: String,
    val city: String,
    val state: String,
    val zip: String,
    val rating: Float,

)
