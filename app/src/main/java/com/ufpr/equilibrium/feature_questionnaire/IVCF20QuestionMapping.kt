package com.ufpr.equilibrium.feature_questionnaire

/**
 * Mapeamento entre IDs locais das questões IVCF-20 e UUIDs da API
 * Baseado no GET /questionnaires/ivcf-20
 */
object IVCF20QuestionMapping {
    
    // ID do questionário IVCF-20 no backend (real UUID from API)
    const val QUESTIONNAIRE_ID = "22fc9b82-da87-479b-843d-8981921f252a"
    
    // Mapeamento: ID Local -> UUID da API
    val questionIdMap = mapOf(
        1 to "512c1ba0-b3d3-434b-afe5-e3d9f8b344b8",  // Idade
        2 to "1a294ad8-0b70-4669-97e2-f8366a60341d",  // Autopercepção
        3 to "d1dc6702-a92e-4e10-8750-db767c47a02f",  // AVD - Compras
        4 to "ba2f1b92-ab33-440a-b06a-c9a280e94411",  // AVD - Dinheiro
        5 to "bbdd4de1-111f-4be7-9bb5-c16cd53f2f8d",  // AVD - Tarefas
        6 to "71c03195-dad6-4560-9df2-0dda4140e5ae"   // AVD - Banho
    )
    
    // Mapeamento de opções para cada questão
    // Formato: Map<QuestionLocalId, Map<OptionIndex, OptionUUID>>
    val optionIdMap = mapOf(
        // Q1: Idade (3 opções)
        1 to mapOf(
            0 to "09086cb8-0f47-4a15-9f2f-0f953dd6d1e2",  // 60-74 anos
            1 to "4fc15a8a-1c4c-45f9-9b9a-b360cd69d93c",  // 75-84 anos
            2 to "af38bb52-d80c-4ece-aa20-e451236cd5cb"   // ≥85 anos
        ),
        
        // Q2: Autopercepção (2 opções)
        2 to mapOf(
            0 to "8923526d-eb2f-4777-9433-8f58222575b8",  // Excelente/boa
            1 to "fea42889-828b-4f09-b1ce-07c7cb876c0d"   // Regular/ruim
        ),
        
        // Q3: AVD - Compras (2 opções)
        3 to mapOf(
            0 to "cfa201de-946a-42b7-9b7a-5089a7f985b8",  // Não
            1 to "1feba158-8b40-4c10-a319-4fbff43acc60"   // Sim
        ),
        
        // Q4: AVD - Dinheiro (2 opções)
        4 to mapOf(
            0 to "58aa6ce1-a71d-4394-8c80-07247ccf9ad5",  // Não
            1 to "ef52613b-c694-4e11-9827-9eec17f23688"   // Sim
        ),
        
        // Q5: AVD - Tarefas (2 opções)
        5 to mapOf(
            0 to "cd023c02-81d8-4269-a1f7-ab5954ca48fb",  // Não
            1 to "cbc5aa75-29f8-4821-aa4b-f84588125bbf"   // Sim  
        ),
        
        // Q6: AVD - Banho (2 opções)
        6 to mapOf(
            0 to "3728721e-af34-47c3-96a6-57720257211c",  // Não
            1 to "e70bfd5b-d716-4255-a56a-3ff2903a2a8b"   // Sim
        )
    )
    
    /**
     * Obtém o UUID da API para uma questão local
     */
    fun getQuestionUUID(localId: Int): String? {
        return questionIdMap[localId]
    }
    
    /**
     * Obtém o UUID da API para uma opção
     */
    fun getOptionUUID(localQuestionId: Int, localOptionIndex: Int): String? {
        return optionIdMap[localQuestionId]?.get(localOptionIndex)
    }
}
