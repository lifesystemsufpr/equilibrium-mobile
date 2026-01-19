package com.ufpr.equilibrium.domain.model

import java.util.Date
import java.util.UUID

/**
 * Domain model representing an evaluation/test session.
 */
data class Evaluation(
    val id: UUID?,
    val patientId: UUID,
    val professionalId: UUID,
    val testType: String,
    val date: Date,
    val results: TestResult
)

/**
 * Domain model for test results.
 */
data class TestResult(
    val testType: String,
    val score: Double?,
    val time: Double?,
    val repetitions: Int?,
    val observations: String?,
    val metadata: Map<String, Any> = emptyMap()
)
