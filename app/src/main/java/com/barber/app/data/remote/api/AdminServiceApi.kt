package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.AdminCreateServiceRequest
import com.barber.app.data.remote.dto.AdminUpdateServiceRequest
import com.barber.app.data.remote.dto.ServiceResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminServiceApi {

    @GET("admin/services")
    suspend fun getAllServices(): List<ServiceResponse>

    @POST("admin/services")
    suspend fun createService(@Body request: AdminCreateServiceRequest): ServiceResponse

    @PUT("admin/services/{id}")
    suspend fun updateService(
        @Path("id") id: Long,
        @Body request: AdminUpdateServiceRequest,
    ): ServiceResponse

    @DELETE("admin/services/{id}")
    suspend fun deleteService(@Path("id") id: Long)
}
