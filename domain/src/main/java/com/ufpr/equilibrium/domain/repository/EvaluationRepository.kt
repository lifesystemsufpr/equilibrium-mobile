package com.ufpr.equilibrium.domain.repository

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Evaluation
import com.ufpr.equilibrium.domain.model.TestResult
import java.util.UUID

/**
 * Repository contract for Evaluation data operations.
 */
interface EvaluationRepository {
    
    /**
     * Get all evaluations for a specific patient.
     */
    suspend fun getEvaluationsForPatient(patientId: UUID): Result<List<Evaluation>>
    
    /**
     * Create a new evaluation.
     */
    suspend fun createEvaluation(evaluation: Evaluation): Result<Evaluation>
    
    /**
     * Submit a test result.
     */
    suspend fun submitTestResult(testResult: TestResult): Result<TestResult>
}
