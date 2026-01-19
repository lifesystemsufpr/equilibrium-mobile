package com.ufpr.equilibrium.feature_teste

import kotlin.math.sqrt

/**
 * Representa as features estatísticas extraídas de uma janela de dados
 * 
 * @property energy soma dos quadrados (Σx²) - indica intensidade do movimento
 * @property variance dispersão dos valores - indica variabilidade
 * @property mean média do sinal - indica tendência central
 * @property rms raiz quadrada média - magnitude efetiva do sinal
 */
data class SignalFeatures(
    val energy: Double,
    val variance: Double,
    val mean: Double,
    val rms: Double
)

/**
 * Extrator de features estatísticas para análise de sinais de sensores
 * Todas as operações são O(n) onde n é o tamanho da janela
 */
object FeatureExtractor {
    
    /**
     * Extrai features de uma janela de dados
     * 
     * @param window lista de valores da janela deslizante
     * @return SignalFeatures com estatísticas calculadas
     */
    fun extract(window: List<Double>): SignalFeatures {
        if (window.isEmpty()) {
            return SignalFeatures(0.0, 0.0, 0.0, 0.0)
        }
        
        val n = window.size.toDouble()
        
        // Média
        val mean = window.sum() / n
        
        // Energia (soma dos quadrados)
        val energy = window.fold(0.0) { acc, x -> acc + x * x }
        
        // Variância
        val variance = if (n > 1) {
            window.fold(0.0) { acc, x -> 
                val diff = x - mean
                acc + diff * diff
            } / (n - 1)
        } else {
            0.0
        }
        
        // RMS (Root Mean Square)
        val rms = sqrt(energy / n)
        
        return SignalFeatures(
            energy = energy,
            variance = variance,
            mean = mean,
            rms = rms
        )
    }
    
    /**
     * Calcula a magnitude 3D (norma euclidiana) de um vetor
     * Útil para combinar os 3 eixos do acelerômetro ou giroscópio
     */
    fun magnitude3D(x: Double, y: Double, z: Double): Double {
        return sqrt(x * x + y * y + z * z)
    }
    
    /**
     * Calcula a magnitude 2D (para análise de plano específico)
     */
    fun magnitude2D(x: Double, y: Double): Double {
        return sqrt(x * x + y * y)
    }
}
