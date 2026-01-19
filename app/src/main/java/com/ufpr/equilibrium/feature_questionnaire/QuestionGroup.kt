package com.ufpr.equilibrium.feature_questionnaire

data class QuestionGroup(
    val id: String,
    val name: String,
    val description: String? = null,
    val maxScore: Int // pontuação máxima do grupo
)
