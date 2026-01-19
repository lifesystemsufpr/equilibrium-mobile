package com.ufpr.equilibrium.domain.model

/**
 * IVCF-20 Questionnaire constants and questions.
 * This object contains the structured questionnaire data for the IVCF-20 assessment.
 */
object IVCF20Questions {
    
    // Question IDs - by group
    const val Q_IDADE = 1
    const val Q_AUTOPERCEPCAO = 2
    const val Q_AVD_COMPRAS = 3
    const val Q_AVD_DINHEIRO = 4
    const val Q_AVD_TAREFAS = 5
    const val Q_AVD_BANHO = 6
    
    // Group IDs
    const val GROUP_IDADE = "idade"
    const val GROUP_AUTOPERCEPCAO = "autopercepcao"
    const val GROUP_AVD = "avd"
    const val GROUP_AVD_INSTRUMENTAL = "avd_instrumental" // special subgroup
    
    val groups = listOf(
        QuestionGroup(
            id = GROUP_IDADE,
            title = "Idade",
            maxScore = 3
        ),
        QuestionGroup(
            id = GROUP_AUTOPERCEPCAO,
            title = "Autopercepção da Saúde",
            maxScore = 1
        ),
        QuestionGroup(
            id = GROUP_AVD,
            title = "Incapacidades Funcionais",
            maxScore = 10
        )
    )
    
    val firstThreeGroupsQuestions = listOf(
        // ========== GROUP 1: AGE ==========
        Question(
            id = Q_IDADE,
            text = "Qual é a sua idade?",
            groupId = GROUP_IDADE,
            options = listOf(
                QuestionOption("60 a 74 anos", 0),
                QuestionOption("75 a 84 anos", 1),
                QuestionOption("≥ 85 anos", 3)
            )
        ),
        
        // ========== GROUP 2: HEALTH SELF-PERCEPTION ==========
        Question(
            id = Q_AUTOPERCEPCAO,
            text = "Em geral, comparando com outras pessoas de sua idade, você diria que sua saúde é:",
            groupId = GROUP_AUTOPERCEPCAO,
            options = listOf(
                QuestionOption("Excelente, muito boa ou boa", 0),
                QuestionOption("Regular ou ruim", 1)
            )
        ),
        
        // ========== GROUP 3: FUNCTIONAL DISABILITIES ==========
        // Subgroup: Instrumental AVD (maximum 4 shared points among the 3 questions)
        Question(
            id = Q_AVD_COMPRAS,
            text = "Por causa de sua saúde ou condição física, você deixou de fazer compras?",
            groupId = GROUP_AVD,
            options = listOf(
                QuestionOption("Não (ou não faz compras por outros motivos que não a saúde)", 0),
                QuestionOption("Sim", 4)
            )
        ),
        
        Question(
            id = Q_AVD_DINHEIRO,
            text = "Por causa de sua saúde ou condição física, você deixou de controlar seu dinheiro, gastos ou pagar as contas de sua casa?",
            groupId = GROUP_AVD,
            options = listOf(
                QuestionOption("Não (ou não controla o dinheiro por outros motivos que não a saúde)", 0),
                QuestionOption("Sim", 4)
            )
        ),
        
        Question(
            id = Q_AVD_TAREFAS,
            text = "Por causa de sua saúde ou condição física, você deixou de realizar pequenos trabalhos domésticos, como lavar louça, arrumar a casa ou fazer limpeza leve?",
            groupId = GROUP_AVD,
            options = listOf(
                QuestionOption("Não (ou não faz mais pequenos trabalhos domésticos por outros motivos que não a saúde)", 0),
                QuestionOption("Sim", 4)
            )
        ),
        
        // Subgroup: Basic AVD
        Question(
            id = Q_AVD_BANHO,
            text = "Por causa de sua saúde ou condição física, você deixou de tomar banho sozinho?",
            groupId = GROUP_AVD,
            options = listOf(
                QuestionOption("Não", 0),
                QuestionOption("Sim", 6)
            )
        )
    )
    
    // IDs of questions that are part of the Instrumental AVD group (special rule)
    val avdInstrumentalQuestionIds = setOf(Q_AVD_COMPRAS, Q_AVD_DINHEIRO, Q_AVD_TAREFAS)
}
