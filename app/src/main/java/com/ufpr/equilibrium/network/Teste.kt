package com.ufpr.equilibrium.network

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * DTO para submissão de teste/avaliação.
 * IMPORTANTE: Todos os campos possuem @SerializedName para garantir
 * compatibilidade com R8/ProGuard em builds de produção.
 */
data class Teste (
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("healthProfessionalId")
    val healthProfessionalId: String,
    
    @SerializedName("participantId")
    val participantId: String,
    
    @SerializedName("healthcareUnitId")
    val healthcareUnitId: String?,
    
    @SerializedName("date")
    val date: java.util.Date,
    
    @SerializedName("totalTime")
    val totalTime: String,
    
    @SerializedName("time_end")
    val time_end: String,
    
    @SerializedName("time_init")
    val time_init: String,
    
    @SerializedName("sensorData")
    val sensorData: List<SensorDataPoint>  // Tipo seguro, não Map<String, Any>
)
