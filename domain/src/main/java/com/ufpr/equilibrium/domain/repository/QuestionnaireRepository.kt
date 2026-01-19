package com.ufpr.equilibrium.domain.repository

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Answer
import com.ufpr.equilibrium.domain.model.Question
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for Questionnaire data operations.
 */
interface QuestionnaireRepository {
    
    /**
     * Load questions for a specific questionnaire.
     */
    fun loadQuestions(questionnaireId: String): Flow<List<Question>>
    
    /**
     * Save an answer to a question.
     */
    suspend fun saveAnswer(answer: Answer): Result<Unit>
    
    /**
     * Get all answers for a questionnaire.
     */
    suspend fun getAnswers(questionnaireId: String): Result<List<Answer>>
    
    /**
     * Clear all answers for a questionnaire.
     */
    suspend fun clearAnswers(questionnaireId: String): Result<Unit>
}
