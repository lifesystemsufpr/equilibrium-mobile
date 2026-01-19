package com.ufpr.equilibrium.feature_questionnaire.payloads

import com.google.gson.annotations.SerializedName

/**
 * Request payload para enviar as respostas do questionário
 * POST /questionnaires/response
 */
data class QuestionnaireResponseRequest(
    @SerializedName("participantId")
    val participantId: String,
    
    @SerializedName("healthProfessionalId")
    val healthProfessionalId: String,
    
    @SerializedName("questionnaireId")
    val questionnaireId: String,
    
    @SerializedName("answers")
    val answers: List<AnswerRequest>
)

/**
 * Representa uma resposta individual a uma questão
 */
data class AnswerRequest(
    @SerializedName("questionId")
    val questionId: String,
    
    @SerializedName("selectedOptionId")
    val selectedOptionId: String,
    
    @SerializedName("valueText")
    val valueText: String? = null
)
