package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.AdminClientResponse
import com.barber.app.data.remote.dto.AdminUpdateClientRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminClientApi {

    @GET("admin/clients")
    suspend fun getAllClients(): List<AdminClientResponse>

    @GET("admin/clients/{id}")
    suspend fun getClientById(@Path("id") id: Long): AdminClientResponse

    @PUT("admin/clients/{id}")
    suspend fun updateClient(
        @Path("id") id: Long,
        @Body request: AdminUpdateClientRequest,
    ): AdminClientResponse
}
