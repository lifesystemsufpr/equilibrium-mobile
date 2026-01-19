package com.ufpr.equilibrium.domain.usecase.questionnaire

import com.ufpr.equilibrium.domain.model.Question
import com.ufpr.equilibrium.domain.repository.QuestionnaireRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to load questions for a questionnaire.
 */
class LoadQuestionsUseCase @Inject constructor(
    private val repository: QuestionnaireRepository
) {
    operator fun invoke(questionnaireId: String): Flow<List<Question>> {
        return repository.loadQuestions(questionnaireId)
    }
}
