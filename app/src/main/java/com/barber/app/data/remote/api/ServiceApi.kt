package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.ServiceResponse
import retrofit2.http.GET

interface ServiceApi {

    @GET("services")
    suspend fun getAllServices(): List<ServiceResponse>
}
