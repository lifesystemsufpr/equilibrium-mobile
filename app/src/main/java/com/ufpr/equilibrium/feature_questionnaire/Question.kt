package com.ufpr.equilibrium.feature_questionnaire

data class Question (
    val id: String,  // UUID da API
    val text: String,
    val options: List<Option>,
    val allowNote: Boolean = false, // se precisa de campo de observação
    val groupId: String? = null, // identificador do grupo (ex: "idade", "autopercepção", "avd_instrumental")
    val groupName: String? = null, // nome do grupo para exibição
    val specialScoring: Boolean = false, // indica se tem regra especial de pontuação
    val order: Int = 0 // ordem da questão conforme retornado pela API
)