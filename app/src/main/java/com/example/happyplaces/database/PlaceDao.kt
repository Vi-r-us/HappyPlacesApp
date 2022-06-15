package com.example.happyplaces.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert
    suspend fun insert(placeEntity: PlaceEntity)

    @Delete
    suspend fun delete(placeEntity: PlaceEntity)

    @Update
    suspend fun update(placeEntity: PlaceEntity)

    @Query("SELECT * FROM `places-table`")
    fun fetchAllHistory(): Flow<List<PlaceEntity>>

    @Query("DELETE FROM `places-table`")
    suspend fun deleteAllHistory()

}