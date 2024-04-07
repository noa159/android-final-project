package com.project.accommodations.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.accommodations.model.Accommodation


@Dao
interface AccommodationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(accommodation: Accommodation): Unit

    @Update
    suspend fun update(accommodation: Accommodation): Unit

    @Query("DELETE FROM accommodations WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM accommodations ORDER BY date DESC")
    fun getAllOrderedByDateDesc(): LiveData<List<Accommodation>>

    @Query("SELECT * FROM accommodations WHERE email = :email")
    fun getByEmail(email: String): LiveData<List<Accommodation>>

    @Query("SELECT * FROM accommodations WHERE id = :id")
    fun getById(id: String): LiveData<Accommodation>
}