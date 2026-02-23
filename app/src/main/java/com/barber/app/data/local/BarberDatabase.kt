package com.barber.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.barber.app.data.local.dao.BarberDao
import com.barber.app.data.local.dao.BookingDao
import com.barber.app.data.local.dao.ServiceDao
import com.barber.app.data.local.entity.BarberEntity
import com.barber.app.data.local.entity.BookingEntity
import com.barber.app.data.local.entity.BookingServiceDetailEntity
import com.barber.app.data.local.entity.ServiceEntity

@Database(
    entities = [
        BarberEntity::class,
        ServiceEntity::class,
        BookingEntity::class,
        BookingServiceDetailEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class BarberDatabase : RoomDatabase() {
    abstract fun barberDao(): BarberDao
    abstract fun serviceDao(): ServiceDao
    abstract fun bookingDao(): BookingDao
}
