package com.barber.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.barber.app.data.local.entity.BarberEntity

@Dao
interface BarberDao {

    @Query("SELECT * FROM barbers WHERE active = 1")
    suspend fun getActiveBarbers(): List<BarberEntity>

    @Query("SELECT * FROM barbers WHERE codigoBarbero = :id")
    suspend fun getBarberById(id: Long): BarberEntity?

    @Upsert
    suspend fun upsertAll(barbers: List<BarberEntity>)

    @Query("DELETE FROM barbers")
    suspend fun clearAll()
}
