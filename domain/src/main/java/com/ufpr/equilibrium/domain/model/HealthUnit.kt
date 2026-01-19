package com.ufpr.equilibrium.domain.model

import java.util.UUID

/**
 * Domain model representing a Health Unit.
 */
data class HealthUnit(
    val id: UUID,
    val name: String,
    val address: String?,
    val city: String?,
    val state: String?
)
