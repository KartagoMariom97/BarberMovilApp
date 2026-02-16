package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.ClientBookingSummaryResponse
import com.barber.app.data.remote.dto.ClientProfileResponse
import com.barber.app.data.remote.dto.ClientResponse
import com.barber.app.data.remote.dto.CreateClientUserRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientApi {

    @POST("clients/user")
    suspend fun createClientUser(@Body request: CreateClientUserRequest): ClientResponse

    @GET("clients/login")
    suspend fun loginByEmail(@Query("email") email: String): ClientProfileResponse

    @GET("clients")
    suspend fun getAllClients(): List<ClientResponse>

    @GET("clients/{clientId}/profile")
    suspend fun getClientProfile(@Path("clientId") clientId: Long): ClientProfileResponse

    @GET("clients/{clientId}/bookings")
    suspend fun getClientBookings(@Path("clientId") clientId: Long): List<ClientBookingSummaryResponse>
}
