package com.ufpr.equilibrium.domain.model

import java.util.UUID

/**
 * Domain model representing a Patient entity.
 * This is the pure business representation, independent of data sources.
 */
data class Patient(
    val id: UUID,
    val cpf: String,
    val fullName: String,
    val phone: String,
    val birthDate: String,
    val gender: String,
    val age: Int,
    val education: String?,
    val socioeconomicLevel: String?,
    val weight: Int?,
    val height: Int?,
    val hasFallHistory: Boolean,
    val address: Address?
)

data class Address(
    val zipCode: String,
    val street: String,
    val number: Int,
    val complement: String?,
    val neighborhood: String,
    val city: String,
    val state: String,
    val stateCode: String
)
