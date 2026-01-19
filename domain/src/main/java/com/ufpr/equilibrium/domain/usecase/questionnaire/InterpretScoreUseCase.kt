package com.ufpr.equilibrium.domain.usecase.questionnaire

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.VulnerabilityLevel
import javax.inject.Inject

/**
 * Use case to interpret the IVCF-20 score into vulnerability level.
 */
class InterpretScoreUseCase @Inject constructor() {
    
    operator fun invoke(totalScore: Int): Result<String> {
        return try {
            val interpretation = when (VulnerabilityLevel.fromScore(totalScore)) {
                VulnerabilityLevel.LOW -> "Baixa vulnerabilidade clínico-funcional"
                VulnerabilityLevel.MODERATE -> "Moderada vulnerabilidade clínico-funcional"
                VulnerabilityLevel.HIGH -> "Alta vulnerabilidade clínico-funcional"
            }
            Result.Success(interpretation)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
