package com.ufpr.equilibrium.feature_questionnaire.payloads

import com.google.gson.annotations.SerializedName

/**
 * Response payload para listar as respostas de um participante
 * GET /questionnaires/participant/{participantId}
 */
data class QuestionnaireResponsesResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("participantId")
    val participantId: String,
    
    @SerializedName("healthProfessionalId")
    val healthProfessionalId: String,
    
    @SerializedName("questionnaireId")
    val questionnaireId: String,
    
    @SerializedName("questionnaireName")
    val questionnaireName: String?,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("totalScore")
    val totalScore: Int?,
    
    @SerializedName("answers")
    val answers: List<AnswerResponse>
)

/**
 * Representa uma resposta individual retornada pela API
 */
data class AnswerResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("questionId")
    val questionId: String,
    
    @SerializedName("questionText")
    val questionText: String?,
    
    @SerializedName("selectedOptionId")
    val selectedOptionId: String,
    
    @SerializedName("selectedOptionText")
    val selectedOptionText: String?,
    
    @SerializedName("score")
    val score: Int?
)

/**
 * Response para a submissão de respostas
 * POST /questionnaires/response
 */
data class QuestionnaireSubmitResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("participantId")
    val participantId: String,
    
    @SerializedName("healthProfessionalId")
    val healthProfessionalId: String,
    
    @SerializedName("questionnaireId")
    val questionnaireId: String,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("totalScore")
    val totalScore: Int?,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Response para obter uma resposta específica
 * GET /questionnaires/response/{responseId}
 */
data class QuestionnaireDetailResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("participantId")
    val participantId: String,
    
    @SerializedName("participant")
    val participant: ParticipantDto?,
    
    @SerializedName("healthProfessionalId")
    val healthProfessionalId: String,
    
    @SerializedName("healthProfessional")
    val healthProfessional: HealthProfessionalDto?,
    
    @SerializedName("questionnaireId")
    val questionnaireId: String,
    
    @SerializedName("questionnaire")
    val questionnaire: QuestionnaireDto?,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("totalScore")
    val totalScore: Int?,
    
    @SerializedName("answers")
    val answers: List<AnswerResponse>
)

/**
 * DTO do participante
 */
data class ParticipantDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("cpf")
    val cpf: String?,
    
    @SerializedName("fullName")
    val fullName: String?
)

/**
 * DTO do profissional de saúde
 */
data class HealthProfessionalDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("cpf")
    val cpf: String?,
    
    @SerializedName("fullName")
    val fullName: String?,
    
    @SerializedName("speciality")
    val speciality: String?
)

/**
 * DTO do questionário
 */
data class QuestionnaireDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?
)
