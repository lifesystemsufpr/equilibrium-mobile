package com.ufpr.equilibrium.feature_questionnaire.payloads

import com.google.gson.annotations.SerializedName

/**
 * Response payload da estrutura do questionário
 * GET /questionnaires/ivcf-20
 */
data class QuestionnaireStructureResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("questions")
    val questions: List<QuestionDto>?
)

/**
 * Representa uma questão do questionário
 */
data class QuestionDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("order")
    val order: Int,
    
    @SerializedName("groupId")
    val groupId: String?,
    
    @SerializedName("groupName")
    val groupName: String?,
    
    @SerializedName("options")
    val options: List<OptionDto>?
)

/**
 * Representa uma opção de resposta
 */
data class OptionDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("score")
    val score: Int
)
