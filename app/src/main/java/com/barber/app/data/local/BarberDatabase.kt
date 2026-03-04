package com.barber.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3, // v3: agrega columna active en services (soft delete)
    exportSchema = false,
)
abstract class BarberDatabase : RoomDatabase() {
    abstract fun barberDao(): BarberDao
    abstract fun serviceDao(): ServiceDao
    abstract fun bookingDao(): BookingDao

    companion object {
        // Migración v1→v2: agrega modification_used — flag único de modificación por cliente
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bookings ADD COLUMN modificationUsed INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migración v2→v3: agrega active en services (soft delete — preserva historial)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE services ADD COLUMN active INTEGER NOT NULL DEFAULT 1")
            }
        }
    }

    /**
     * 🔥 Limpia completamente todas las tablas de la base de datos.
     *
     * ⚠️ IMPORTANTE:
     * - Solo debe llamarse cuando el usuario hace LOGOUT.
     * - NO debe ejecutarse cuando la app se cierra.
     * - NO debe ejecutarse automáticamente en 401.
     *
     * Esto permite:
     * ✔ Mantener caché local si la app se reinicia
     * ✔ Forzar sincronización completa cuando se cierra sesión
     */
    suspend fun clearAllTablesSafe() {
        withTransaction {
            clearAllTables()
        }
    }

}
