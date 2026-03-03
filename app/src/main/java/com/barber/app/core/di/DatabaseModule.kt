package com.barber.app.core.di

import android.content.Context
import androidx.room.Room
import com.barber.app.data.local.BarberDatabase
import com.barber.app.data.local.dao.BarberDao
import com.barber.app.data.local.dao.BookingDao
import com.barber.app.data.local.dao.ServiceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BarberDatabase =
        Room.databaseBuilder(context, BarberDatabase::class.java, "barber_db")
            // Migración controlada v1→v2: agrega columnas modifiedByClient y modificationRejected
            .addMigrations(BarberDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideBarberDao(db: BarberDatabase): BarberDao = db.barberDao()

    @Provides
    fun provideServiceDao(db: BarberDatabase): ServiceDao = db.serviceDao()

    @Provides
    fun provideBookingDao(db: BarberDatabase): BookingDao = db.bookingDao()
}
