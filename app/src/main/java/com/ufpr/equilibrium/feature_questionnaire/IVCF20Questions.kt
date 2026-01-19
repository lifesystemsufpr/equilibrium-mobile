package com.ufpr.equilibrium.feature_questionnaire

/**
 * IVCF-20 Questions with real UUIDs from API
 * Fetched from GET /questionnaires/ivcf-20
 */
object IVCF20Questions {
    
    // ID do questionário IVCF-20 no backend
    const val QUESTIONNAIRE_ID = "22fc9b82-da87-479b-843d-8981921f252a"
    
    // UUIDs das questões da API
    const val Q_IDADE = "a2b6a6ae-d5cf-451a-a0fd-7cffade49ab7"
    const val Q_AUTOPERCEPCAO = "5b4fd2d1-7544-466c-a1fb-9b85b5d5a4e4"
    const val Q_AVD_COMPRAS = "c7b6bc21-ba12-4e4b-bd49-5b062f105036"
    const val Q_AVD_DINHEIRO = "36a088bc-44b3-483e-a50e-8543cfa3d72b"
    const val Q_AVD_TAREFAS = "a13f2a9c-a7c7-4c75-98a2-0f662114ff70"
    const val Q_AVD_BANHO = "7403ea44-b4a9-4240-b591-6b68c468915f"
    
    // IDs dos grupos
    const val GROUP_IDADE = "ac569b5e-478d-41dc-b6db-422753584c7d"
    const val GROUP_AUTOPERCEPCAO = "7d5fb21c-ce02-4f59-b015-1fcb58ce9d13"
    const val GROUP_AVD_INSTRUMENTAL = "37b50287-9a08-4f46-9d67-0f96ee81a0d3"
    const val GROUP_AVD_BASICA = "57be0c51-4c20-40ba-988c-1c4f24a965db"
    
    val groups = listOf(
        QuestionGroup(
            id = GROUP_IDADE,
            name = "Idade",
            maxScore = 3
        ),
        QuestionGroup(
            id = GROUP_AUTOPERCEPCAO,
            name = "Autopercepção da Saúde",
            maxScore = 1
        ),
        QuestionGroup(
            id = GROUP_AVD_INSTRUMENTAL,
            name = "Atividades de Vida Diária (AVD Instrumental)",
            maxScore = 4
        ),
        QuestionGroup(
            id = GROUP_AVD_BASICA,
            name = "Atividades de Vida Diária (AVD Básica)",
            maxScore = 6
        )
    )
    
    val firstThreeGroupsQuestions = listOf(
        // ========== GRUPO 1: IDADE ==========
        Question(
            id = Q_IDADE,
            text = "Qual é a sua idade?",
            groupId = GROUP_IDADE,
            groupName = "Idade",
            options = listOf(
                Option("3c49ad52-b130-406c-b882-bc3d8bc65bc4", "60 a 74 anos", 0),
                Option("7eecac35-5729-4868-a232-a1df13edc7ac", "75 a 84 anos", 1),
                Option("23c5a680-7aa2-4cfb-adec-193b0637568d", "≥ 85 anos", 3)
            )
        ),
        
        // ========== GRUPO 2: AUTOPERCEPÇÃO DA SAÚDE ==========
        Question(
            id = Q_AUTOPERCEPCAO,
            text = "Em geral, comparando com outras pessoas de sua idade, você diria que sua saúde é:",
            groupId = GROUP_AUTOPERCEPCAO,
            groupName = "Autopercepção da Saúde",
            options = listOf(
                Option("d0318787-a66a-42c1-b692-aab69c6cb666", "Excelente, muito boa ou boa", 0),
                Option("5752f3ab-0c18-418d-96d5-f1ffbc7fc93d", "Regular ou ruim", 1)
            )
        ),
        
        // ========== GRUPO 3: AVD INSTRUMENTAL ==========
        Question(
            id = Q_AVD_COMPRAS,
            text = "Por causa de sua saúde ou condição física, você deixou de fazer compras?",
            groupId = GROUP_AVD_INSTRUMENTAL,
            groupName = "Atividades de Vida Diária (AVD Instrumental)",
            specialScoring = true,
            options = listOf(
                Option("f89837c7-0c98-443d-89f2-736c5a13ca37", "Não (ou não faz por outros motivos)", 0),
                Option("a416f0b0-560c-44a7-a19c-cc124d239e66", "Sim", 4)
            )
        ),
        
        Question(
            id = Q_AVD_DINHEIRO,
            text = "Por causa de sua saúde ou condição física, você deixou de controlar seu dinheiro, gastos ou pagar as contas de sua casa?",
            groupId = GROUP_AVD_INSTRUMENTAL,
            groupName = "Atividades de Vida Diária (AVD Instrumental)",
            specialScoring = true,
            options = listOf(
                Option("83354438-e952-4cde-8c9c-7f9f3ae10f12", "Não (ou não controla por outros motivos)", 0),
                Option("1bc40948-52a2-4f40-8e8b-2e758e1860d8", "Sim", 4)
            )
        ),
        
        Question(
            id = Q_AVD_TAREFAS,
            text = "Por causa de sua saúde ou condição física, você deixou de realizar pequenos trabalhos domésticos, como lavar louça, arrumar a casa ou fazer limpeza leve?",
            groupId = GROUP_AVD_INSTRUMENTAL,
            groupName = "Atividades de Vida Diária (AVD Instrumental)",
            specialScoring = true,
            options = listOf(
                Option("cbedcf04-7b85-4075-a447-9813a65db3b1", "Não (ou não faz por outros motivos)", 0),
                Option("d183325e-f1b3-4d8c-98b6-5f8b76f8d78c", "Sim", 4)
            )
        ),
        
        // ========== GRUPO 4: AVD BÁSICA ==========
        Question(
            id = Q_AVD_BANHO,
            text = "Por causa de sua saúde ou condição física, você deixou de tomar banho sozinho?",
            groupId = GROUP_AVD_BASICA,
            groupName = "Atividades de Vida Diária (AVD Básica)",
            options = listOf(
                Option("3d7e4577-1f0e-4455-abb0-fb3820ed6f78", "Não", 0),
                Option("4939a981-168e-4b6e-ac14-f994b2278fac", "Sim", 6)
            )
        )
    )
    
    // IDs das questões que fazem parte do grupo AVD Instrumental (regra especial)
    val avdInstrumentalQuestionIds = setOf(Q_AVD_COMPRAS, Q_AVD_DINHEIRO, Q_AVD_TAREFAS)
}
