package com.ufpr.equilibrium.domain.repository

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.HealthUnit

/**
 * Repository contract for HealthUnit data operations.
 */
interface HealthUnitRepository {
    
    /**
     * Get all health units.
     */
    suspend fun getHealthUnits(): Result<List<HealthUnit>>
}
