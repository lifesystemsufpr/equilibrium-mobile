package com.ufpr.equilibrium.domain.usecase.questionnaire

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Answer
import com.ufpr.equilibrium.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Use case to save an answer to a question.
 */
class SaveAnswerUseCase @Inject constructor(
    private val repository: QuestionnaireRepository
) {
    suspend operator fun invoke(answer: Answer): Result<Unit> {
        return repository.saveAnswer(answer)
    }
}
