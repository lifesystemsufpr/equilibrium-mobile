package com.ufpr.equilibrium.feature_questionnaire

/**
 * IVCF-20 Constants
 * 
 * Este arquivo contém apenas constantes de referência para o questionário IVCF-20.
 * As questões são carregadas dinamicamente da API através do endpoint:
 * GET /questionnaires/ivcf-20
 * 
 * @see QuestionnaireViewModel.loadQuestions
 * @see com.ufpr.equilibrium.feature_questionnaire.repository.QuestionnaireRepository.getIVCF20QuestionnaireStructure
 */
object IVCF20Questions {
    
    /**
     * ID do questionário IVCF-20 no backend
     * Este UUID identifica o questionário na API
     */
    const val QUESTIONNAIRE_ID = "22fc9b82-da87-479b-843d-8981921f252a"
}
