package com.ufpr.equilibrium.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Patient data from API.
 */
data class PatientDto(
    @SerializedName("id") val id: String?,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("cpf") val cpf: String,
    @SerializedName("age") val age: Int,
    @SerializedName("weight") val weight: Float?,
    @SerializedName("height") val height: Int?,
    @SerializedName("downFall") val downFall: Boolean
)

/**
 * DTO for Patient registration/creation.
 * Note: user field is nullable because API response may not include it.
 */
data class PatientRegistrationDto(
    @SerializedName("user") val user: UserDto?,
    @SerializedName("birthday") val birthday: String,
    @SerializedName("weight") val weight: Int,
    @SerializedName("height") val height: Int?,
    @SerializedName("zipCode") val zipCode: String,
    @SerializedName("street") val street: String,
    @SerializedName("number") val number: String,
    @SerializedName("complement") val complement: String,
    @SerializedName("neighborhood") val neighborhood: String,
    @SerializedName("scholarship") val scholarship: String,
    @SerializedName("socio_economic_level") val socioEconomicLevel: String,
    @SerializedName("city") val city: String,
    @SerializedName("state") val state: String
)

/**
 * DTO for User data (used in patient registration).
 */
data class UserDto(
    @SerializedName("id") val id: String?,
    @SerializedName("cpf") val cpf: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("password") val password: String?,
    @SerializedName("phone") val phone: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("role") val role: String
)
