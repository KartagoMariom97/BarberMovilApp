package com.barber.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.barber.app.data.local.entity.ServiceEntity

@Dao
interface ServiceDao {

    @Query("SELECT * FROM services")
    suspend fun getAllServices(): List<ServiceEntity>

    @Upsert
    suspend fun upsertAll(services: List<ServiceEntity>)

    @Query("DELETE FROM services")
    suspend fun clearAll()
}
