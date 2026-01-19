package com.ufpr.equilibrium.feature_professional

import com.google.gson.annotations.SerializedName

/**
 * DTO para dados de profissional de saúde.
 * IMPORTANTE: @SerializedName garante que ProGuard não ofusque os nomes dos campos.
 */
data class ProfessionalModel (
    @SerializedName("cpf")
    val cpf: String,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("gender")
    val gender: String,
    
    @SerializedName("profile")
    val profile: String,
    
    @SerializedName("expertise")
    val expertise: String,
    
    @SerializedName("email")
    val email: String
)
