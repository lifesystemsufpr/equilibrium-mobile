package com.ufpr.equilibrium.domain.model

/**
 * Domain model representing an answer to a question.
 */
data class Answer(
    val questionId: Int,
    val selectedOptionIndex: Int,
    val score: Int,
    val note: String? = null
)

/**
 * Represents the total score calculated from a questionnaire.
 */
data class QuestionnaireScore(
    val totalScore: Int,
    val groupScores: Map<String, Int>,
    val vulnerabilityLevel: VulnerabilityLevel
)

/**
 * Vulnerability level classification based on IVCF-20 score.
 */
enum class VulnerabilityLevel {
    LOW,      // 0-6 points
    MODERATE, // 7-14 points  
    HIGH;     // 15+ points
    
    companion object {
        fun fromScore(score: Int): VulnerabilityLevel {
            return when {
                score <= 6 -> LOW
                score <= 14 -> MODERATE
                else -> HIGH
            }
        }
    }
}
