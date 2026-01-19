package com.ufpr.equilibrium.network

import com.google.gson.annotations.SerializedName

/**
 * DTO para dados de usuário.
 * IMPORTANTE: @SerializedName garante que ProGuard não ofusque os nomes dos campos.
 */
data class Usuario(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("cpf")
    val cpf: String,
    
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("gender")
    val gender: String,
    
    @SerializedName("role")
    val role: String
)
