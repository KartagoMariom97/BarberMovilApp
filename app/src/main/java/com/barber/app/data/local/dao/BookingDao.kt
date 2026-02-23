package com.barber.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.barber.app.data.local.entity.BookingEntity
import com.barber.app.data.local.entity.BookingServiceDetailEntity

@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings WHERE status NOT IN ('CANCELLED') ORDER BY fechaReserva DESC")
    suspend fun getBookings(): List<BookingEntity>

    @Query("SELECT * FROM booking_service_details WHERE bookingId = :bookingId")
    suspend fun getServiceDetails(bookingId: Long): List<BookingServiceDetailEntity>

    @Upsert
    suspend fun upsertBookings(bookings: List<BookingEntity>)

    @Upsert
    suspend fun upsertServiceDetails(details: List<BookingServiceDetailEntity>)

    @Query("DELETE FROM booking_service_details WHERE bookingId = :bookingId")
    suspend fun deleteServiceDetails(bookingId: Long)

    @Query("UPDATE bookings SET status = 'CANCELLED' WHERE id = :id")
    suspend fun markCancelled(id: Long)

    @Query("DELETE FROM bookings")
    suspend fun clearAll()

    @Query("DELETE FROM booking_service_details")
    suspend fun clearAllDetails()
}
