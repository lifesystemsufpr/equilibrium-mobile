package com.ufpr.equilibrium.feature_professional

import com.google.gson.annotations.SerializedName

/**
 * DTO para dados de usuário.
 * IMPORTANTE: @SerializedName garante que ProGuard não ofusque os nomes dos campos.
 */
data class User (
    @SerializedName("cpf")
    val cpf: String,
    
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("gender")
    val gender: String,
    
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("password")
    val password: String? = null,
    
    @SerializedName("phone")
    val phone: String = "",
    
    @SerializedName("role")
    val role: String = "PARTICIPANT"
)