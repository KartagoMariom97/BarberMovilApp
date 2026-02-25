package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.AdminClientResponse
import com.barber.app.data.remote.dto.AdminCreateClientRequest
import com.barber.app.data.remote.dto.AdminUpdateClientRequest
import com.barber.app.data.remote.dto.AdminUpdateClientStatusRequest
import com.barber.app.data.remote.dto.ClientResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminClientApi {

    @GET("admin/clients")
    suspend fun getAllClients(): List<AdminClientResponse>

    /** Crea un nuevo cliente con cuenta de usuario vía admin */
    @POST("admin/clients")
    suspend fun createClient(@Body request: AdminCreateClientRequest): ClientResponse

    @GET("admin/clients/{id}")
    suspend fun getClientById(@Path("id") id: Long): AdminClientResponse

    @PUT("admin/clients/{id}")
    suspend fun updateClient(
        @Path("id") id: Long,
        @Body request: AdminUpdateClientRequest,
    ): AdminClientResponse

    @PATCH("admin/clients/{id}/status")
    suspend fun updateClientStatus(
        @Path("id") id: Long,
        @Body request: AdminUpdateClientStatusRequest,
    ): AdminClientResponse
}
