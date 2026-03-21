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

    // [F6] Offline queue — devuelve barberos pendientes de sincronizar con el servidor
    @Query("SELECT * FROM barbers WHERE syncedAt IS NULL")
    suspend fun getPendingSync(): List<BarberEntity>

    // [F6] Marca un barbero como sincronizado con timestamp epoch ms
    @Query("UPDATE barbers SET syncedAt = :timestamp WHERE codigoBarbero = :id")
    suspend fun markSynced(id: Long, timestamp: Long)
}
