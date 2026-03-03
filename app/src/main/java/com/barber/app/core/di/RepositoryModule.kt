package com.barber.app.core.di

import com.barber.app.data.repository.AdminBarberRepositoryImpl
import com.barber.app.data.repository.AdminBookingRepositoryImpl
import com.barber.app.data.repository.AdminClientRepositoryImpl
import com.barber.app.data.repository.AdminServiceRepositoryImpl
import com.barber.app.data.repository.AuthRepositoryImpl
import com.barber.app.data.repository.BarberRepositoryImpl
import com.barber.app.data.repository.BookingRepositoryImpl
import com.barber.app.data.repository.ClientRepositoryImpl
import com.barber.app.data.repository.NotificationRepositoryImpl
import com.barber.app.data.repository.ServiceRepositoryImpl
import com.barber.app.domain.repository.AdminBarberRepository
import com.barber.app.domain.repository.AdminBookingRepository
import com.barber.app.domain.repository.AdminClientRepository
import com.barber.app.domain.repository.AdminServiceRepository
import com.barber.app.domain.repository.AuthRepository
import com.barber.app.domain.repository.BarberRepository
import com.barber.app.domain.repository.BookingRepository
import com.barber.app.domain.repository.ClientRepository
import com.barber.app.domain.repository.NotificationRepository
import com.barber.app.domain.repository.ServiceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds
    @Singleton
    abstract fun bindBarberRepository(impl: BarberRepositoryImpl): BarberRepository

    @Binds
    @Singleton
    abstract fun bindServiceRepository(impl: ServiceRepositoryImpl): ServiceRepository

    @Binds
    @Singleton
    abstract fun bindClientRepository(impl: ClientRepositoryImpl): ClientRepository

    @Binds
    @Singleton
    abstract fun bindAdminBarberRepository(impl: AdminBarberRepositoryImpl): AdminBarberRepository

    @Binds
    @Singleton
    abstract fun bindAdminServiceRepository(impl: AdminServiceRepositoryImpl): AdminServiceRepository

    @Binds
    @Singleton
    abstract fun bindAdminClientRepository(impl: AdminClientRepositoryImpl): AdminClientRepository

    @Binds
    @Singleton
    abstract fun bindAdminBookingRepository(impl: AdminBookingRepositoryImpl): AdminBookingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
