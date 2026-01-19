package com.ufpr.equilibrium.feature_healthUnit

import com.google.gson.annotations.SerializedName

/**
 * DTO para unidade de saúde.
 * IMPORTANTE: @SerializedName garante que ProGuard não ofusque os nomes dos campos.
 */
data class HealthUnit(
    @SerializedName("id")
    val id : String,
    
    @SerializedName("name")
    val name: String
)

data class HealthUnitEnvelope(
    @SerializedName("data")
    val data: List<HealthUnit>,
    
    @SerializedName("meta")
    val meta: HealthUnitMeta?
)

data class HealthUnitMeta(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)
