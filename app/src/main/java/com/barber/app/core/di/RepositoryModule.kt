package com.barber.app.core.di

import com.barber.app.data.repository.AuthRepositoryImpl
import com.barber.app.data.repository.BarberRepositoryImpl
import com.barber.app.data.repository.BookingRepositoryImpl
import com.barber.app.data.repository.ClientRepositoryImpl
import com.barber.app.data.repository.ServiceRepositoryImpl
import com.barber.app.domain.repository.AuthRepository
import com.barber.app.domain.repository.BarberRepository
import com.barber.app.domain.repository.BookingRepository
import com.barber.app.domain.repository.ClientRepository
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
}
