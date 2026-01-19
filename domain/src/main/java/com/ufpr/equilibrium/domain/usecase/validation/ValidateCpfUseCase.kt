package com.ufpr.equilibrium.domain.usecase.validation

import com.ufpr.equilibrium.core.common.Result
import javax.inject.Inject

/**
 * Use case to validate CPF format and check digits.
 */
class ValidateCpfUseCase @Inject constructor() {
    
    data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null
    )
    
    operator fun invoke(cpf: String): ValidationResult {
        val cleanCpf = cpf.replace(Regex("[^\\d]"), "")
        
        // Check length
        if (cleanCpf.length != 11) {
            return ValidationResult(false, "CPF inválido")
        }
        
        // Check if all digits are the same (invalid CPF)
        if (cleanCpf.all { it == cleanCpf[0] }) {
            return ValidationResult(false, "CPF inválido")
        }
        
        // Validate check digits
        val isValid = validateCheckDigits(cleanCpf)
        return if (isValid) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "CPF inválido")
        }
    }
    
    private fun validateCheckDigits(cpf: String): Boolean {
        // Calculate first check digit
        var sum = 0
        for (i in 0 until 9) {
            sum += cpf[i].digitToInt() * (10 - i)
        }
        var remainder = sum % 11
        val firstCheckDigit = if (remainder < 2) 0 else 11 - remainder
        
        if (cpf[9].digitToInt() != firstCheckDigit) {
            return false
        }
        
        // Calculate second check digit
        sum = 0
        for (i in 0 until 10) {
            sum += cpf[i].digitToInt() * (11 - i)
        }
        remainder = sum % 11
        val secondCheckDigit = if (remainder < 2) 0 else 11 - remainder
        
        return cpf[10].digitToInt() == secondCheckDigit
    }
}
