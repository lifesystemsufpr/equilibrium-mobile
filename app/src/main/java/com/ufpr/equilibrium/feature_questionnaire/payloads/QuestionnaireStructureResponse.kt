package com.ufpr.equilibrium.feature_questionnaire.payloads

import com.google.gson.annotations.SerializedName

/**
 * Response payload da estrutura do questionário
 * GET /questionnaires/ivcf-20
 * 
 * ESTRUTURA REAL DA API:
 * - Questões estão aninhadas em groups[] e subGroups[]
 * - Usa 'statement' ao invés de 'text' para questões
 * - Usa 'label' ao invés de 'text' para opções
 * - Usa 'title' ao invés de 'name'
 */
data class QuestionnaireStructureResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("slug")
    val slug: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("groups")
    val groups: List<GroupDto>?
)

/**
 * Representa um grupo de questões
 */
data class GroupDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("order")
    val order: Int,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("questions")
    val questions: List<QuestionDto>?,
    
    @SerializedName("subGroups")
    val subGroups: List<SubGroupDto>?
)

/**
 * Representa um subgrupo de questões
 */
data class SubGroupDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("order")
    val order: Int,
    
    @SerializedName("groupId")
    val groupId: String,
    
    @SerializedName("questions")
    val questions: List<QuestionDto>?
)

/**
 * Representa uma questão do questionário
 */
data class QuestionDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("statement")
    val statement: String,
    
    @SerializedName("order")
    val order: Int,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("required")
    val required: Boolean?,
    
    @SerializedName("groupId")
    val groupId: String?,
    
    @SerializedName("subGroupId")
    val subGroupId: String?,
    
    @SerializedName("options")
    val options: List<OptionDto>?
)

/**
 * Representa uma opção de resposta
 */
data class OptionDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("label")
    val label: String,
    
    @SerializedName("score")
    val score: Int,
    
    @SerializedName("order")
    val order: Int
)
