package com.ufpr.equilibrium.domain.usecase.validation

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Use case to validate birth date.
 */
class ValidateBirthDateUseCase @Inject constructor() {
    
    data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null
    )
    
    operator fun invoke(date: String): ValidationResult {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                isLenient = false
            }
            val birthDate = sdf.parse(date) ?: return ValidationResult(false, "Data inválida")
            
            val today = Calendar.getInstance()
            val birthCal = Calendar.getInstance().apply { time = birthDate }
            
            // 1) Do not accept future dates
            if (birthDate.after(today.time)) {
                return ValidationResult(false, "Data não pode ser no futuro")
            }
            
            // 2) Do not accept dates too old (before 01/01/1900)
            val minDate = Calendar.getInstance().apply {
                set(1900, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (birthDate.before(minDate.time)) {
                return ValidationResult(false, "Data muito antiga")
            }
            
            // 3) Do not accept unrealistic ages (> 120 years)
            var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            val beforeBirthday = today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)
            if (beforeBirthday) age--
            
            if (age !in 0..120) {
                return ValidationResult(false, "Idade inválida")
            }
            
            ValidationResult(true)
        } catch (e: Exception) {
            ValidationResult(false, "Data de nascimento inválida (dd/MM/yyyy)")
        }
    }
}
