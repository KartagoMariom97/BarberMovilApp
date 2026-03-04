package com.barber.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.barber.app.data.local.entity.ServiceEntity

@Dao
interface ServiceDao {

    // Solo devuelve servicios activos para uso público (soft-deleted excluidos)
    @Query("SELECT * FROM services WHERE active = 1")
    suspend fun getAllServices(): List<ServiceEntity>

    @Upsert
    suspend fun upsertAll(services: List<ServiceEntity>)

    @Query("DELETE FROM services")
    suspend fun clearAll()
}
