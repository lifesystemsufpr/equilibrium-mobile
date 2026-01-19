package com.ufpr.equilibrium.domain.usecase.patient

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Patient
import com.ufpr.equilibrium.domain.repository.PatientRepository
import javax.inject.Inject

/**
 * Use case to get a patient by CPF.
 */
class GetPatientByCpfUseCase @Inject constructor(
    private val repository: PatientRepository
) {
    suspend operator fun invoke(cpf: String): Result<Patient?> {
        return repository.getPatientByCpf(cpf)
    }
}
