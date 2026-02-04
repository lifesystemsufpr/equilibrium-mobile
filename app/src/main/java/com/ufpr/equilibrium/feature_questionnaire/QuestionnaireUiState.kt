package com.ufpr.equilibrium.feature_questionnaire

/**
 * UI State for Questionnaire screen
 */
data class QuestionnaireUiState (
    val questions: List<Question> = emptyList(),
    val questionnaireId: String? = null,  // Store the real ID from API response
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalScore: Int = 0,
    val interpretation: String = ""
)
