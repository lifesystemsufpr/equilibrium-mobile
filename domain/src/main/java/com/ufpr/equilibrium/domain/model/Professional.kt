package com.ufpr.equilibrium.domain.model

import java.util.UUID

/**
 * Domain model representing a Health Professional entity.
 */
data class Professional(
    val id: UUID?,
    val cpf: String,
    val fullName: String,
    val phone: String,
    val birthDate: String,
    val gender: String,
    val healthUnitId: UUID,
    val address: Address?
)
