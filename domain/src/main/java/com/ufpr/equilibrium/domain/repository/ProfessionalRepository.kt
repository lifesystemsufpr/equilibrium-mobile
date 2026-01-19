package com.ufpr.equilibrium.domain.repository

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Professional

/**
 * Repository contract for Professional data operations.
 */
interface ProfessionalRepository {
    
    /**
     * Create a new health professional.
     */
    suspend fun createProfessional(professional: Professional): Result<Professional>
    
    /**
     * Update existing professional data.
     */
    suspend fun updateProfessional(professional: Professional): Result<Professional>
}
