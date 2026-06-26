package com.example.sincronizacionapp.api

import com.example.sincronizacionapp.data.HealthRecord
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface HealthApiService {
    @POST("api/v1/reloj")
    suspend fun uploadHealthData(@Body data: HealthRecord): Response<Map<String, Any>>

    companion object {
        // Debes cambiar esta IP por la IP local de tu PC (ej. 192.168.1.15)
        private const val BASE_URL = "http://192.168.100.8:3000/"

        fun create(): HealthApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HealthApiService::class.java)
        }
    }
}
