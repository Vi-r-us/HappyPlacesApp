package com.example.happyplaces.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places-table")
data class PlaceEntity(

    // Mandatory Fields
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String?,
    val image: String?,
    val description: String?,
    val date: String?,
    val location: String?,
    val latitude: Double,
    val longitude: Double,

    // Temporary Fields
    val category: String? = "",
    val streetAddress1: String? = "",
    val streetAddress2: String? = "",
    val country: String? = "",
    val state: String? = "",
    val zip: String? = "",
    val rating: Float = 0.0f,
    val isFavorite: Int = 0,
    val phoneNumber: String? = "",
    val emailAddress: String? = "",
    val website: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(category)
        parcel.writeString(streetAddress1)
        parcel.writeString(streetAddress2)
        parcel.writeString(country)
        parcel.writeString(state)
        parcel.writeString(zip)
        parcel.writeFloat(rating)
        parcel.writeInt(isFavorite)
        parcel.writeString(phoneNumber)
        parcel.writeString(emailAddress)
        parcel.writeString(website)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaceEntity> {
        override fun createFromParcel(parcel: Parcel): PlaceEntity {
            return PlaceEntity(parcel)
        }

        override fun newArray(size: Int): Array<PlaceEntity?> {
            return arrayOfNulls(size)
        }
    }
}
