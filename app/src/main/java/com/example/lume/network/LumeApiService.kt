package com.example.lume.network

import com.example.lume.data.StructuralOcrResult
import com.example.lume.data.TxFields
import retrofit2.http.Body
import retrofit2.http.POST

interface LumeApiService {
    /**
     * Stage B: AI-assisted classification.
     * Sends the structural extraction results to the backend.
     */
    @POST("classify")
    suspend fun classifyTransaction(
        @Body structuralResult: StructuralOcrResult
    ): TxFields
}
