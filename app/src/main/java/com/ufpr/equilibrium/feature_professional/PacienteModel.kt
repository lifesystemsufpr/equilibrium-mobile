package com.ufpr.equilibrium.feature_professional

import com.google.gson.annotations.SerializedName

/**
 * DTO para dados de paciente (cadastro).
 * IMPORTANTE: @SerializedName garante que ProGuard não ofusque os nomes dos campos.
 */
data class PacienteModel(
    @SerializedName("user")
    val user: User,
    
    @SerializedName("birthday")
    val birthday: String,
    
    @SerializedName("weight")
    val weight: Int,
    
    @SerializedName("height")
    val height: Int?,
    
    @SerializedName("zipCode")
    val zipCode: String,
    
    @SerializedName("street")
    val street: String,
    
    @SerializedName("number")
    val number: String,
    
    @SerializedName("complement")
    val complement: String,
    
    @SerializedName("neighborhood")
    val neighborhood: String,
    
    @SerializedName("scholarship")
    val scholarship: String,
    
    @SerializedName("socio_economic_level")
    val socioEconomicLevel: String,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("state")
    val state: String
)
