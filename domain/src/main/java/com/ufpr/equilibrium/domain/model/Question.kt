package com.ufpr.equilibrium.domain.model

/**
 * Domain model representing a questionnaire question.
 */
data class Question(
    val id: Int,
    val groupId: String,
    val text: String,
    val options: List<QuestionOption>,
    val allowsNote: Boolean = false,
    val noteHint: String? = null
)

/**
 * Option for a question.
 */
data class QuestionOption(
    val text: String,
    val score: Int
)

/**
 * Grouping of questions.
 */
data class QuestionGroup(
    val id: String,
    val title: String,
    val description: String? = null,
    val maxScore: Int
)
