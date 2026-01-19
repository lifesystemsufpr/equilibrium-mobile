package com.ufpr.equilibrium.feature_questionnaire

data class Answer(
    val selectedOptionIndex: Int,
    val score: Int,
    val note: String? = null
)
