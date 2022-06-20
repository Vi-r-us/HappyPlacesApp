package com.example.happyplaces.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert
    fun insert(placeEntity: PlaceEntity)

    @Delete
    fun delete(placeEntity: PlaceEntity)

    @Update
    fun update(placeEntity: PlaceEntity)

    @Query("SELECT * FROM `places-table`")
    fun fetchAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM `places-table` ORDER BY id DESC LIMIT 1")
    fun getLastInsertedPlace(): Flow<PlaceEntity>

    @Query("SELECT * FROM `places-table` where id=:id")
    fun fetchPlaceById(id: Int): Flow<PlaceEntity>

}