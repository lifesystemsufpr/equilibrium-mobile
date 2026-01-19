package com.ufpr.equilibrium.feature_teste

import android.util.Log

/**
 * Calibrador adaptativo para detecção SLS
 * 
 * Durante os primeiros segundos do teste, coleta dados para:
 * - Estimar a energia média do sinal em repouso (baseline)
 * - Calcular thresholds dinâmicos para movimento e estabilidade
 * 
 * Isso permite adaptação às características do usuário e dispositivo
 */
class SlsCalibrator(
    private val calibrationDurationMs: Long = 2000L,
    private val minSamplesForCalibration: Int = 30  // ~1.2s a 25Hz
) {
    
    companion object {
        private const val TAG = "SlsCalibrator"
        
        // Multiplicadores para calcular thresholds a partir do baseline
        private const val MOVEMENT_THRESHOLD_MULTIPLIER = 2.0   // Reduçdo de 2.5
        private const val STABILITY_THRESHOLD_MULTIPLIER = 1.2  // Reduzido de 1.3
        
        // Valores default caso calibração não funcione
        private const val DEFAULT_MOVEMENT_THRESHOLD = 0.8    // Reduzido de 1.5
        private const val DEFAULT_STABILITY_THRESHOLD = 0.3   // Reduzido de 0.5
        
        // Limites para thresholds calculados - mais sensíveis
        private const val MIN_MOVEMENT_THRESHOLD = 0.4   // Reduzido de 0.8
        private const val MAX_MOVEMENT_THRESHOLD = 3.0   // Reduzido de 4.0
        private const val MIN_STABILITY_THRESHOLD = 0.15 // Reduzido de 0.2
        private const val MAX_STABILITY_THRESHOLD = 0.8  // Reduzido de 1.0
    }
    
    /**
     * Indica se a calibração foi concluída
     */
    var isCalibrated: Boolean = false
        private set
    
    /**
     * Energia média observada durante calibração (baseline)
     */
    var baselineEnergy: Double = 0.0
        private set
    
    /**
     * Threshold para detectar início de movimento
     * Energia acima deste valor indica transição
     */
    var movementThreshold: Double = DEFAULT_MOVEMENT_THRESHOLD
        private set
    
    /**
     * Threshold para detectar estabilidade
     * Energia abaixo deste valor indica posição estática
     */
    var stabilityThreshold: Double = DEFAULT_STABILITY_THRESHOLD
        private set
    
    // Dados de calibração
    private val gyroSamples = mutableListOf<Double>()
    private val accelSamples = mutableListOf<Double>()
    private var calibrationStartTimeMs: Long = 0L
    
    /**
     * Adiciona uma amostra durante o período de calibração
     * 
     * @param gyroMagnitude magnitude do giroscópio (rad/s)
     * @param accelMagnitude magnitude do acelerômetro (m/s²)
     * @param timestampMs timestamp atual
     */
    fun addSample(gyroMagnitude: Double, accelMagnitude: Double, timestampMs: Long) {
        if (isCalibrated) return
        
        // Inicializar timestamp de início
        if (calibrationStartTimeMs == 0L) {
            calibrationStartTimeMs = timestampMs
            Log.d(TAG, "Iniciando calibração...")
        }
        
        // Coletar amostras
        gyroSamples.add(gyroMagnitude)
        accelSamples.add(accelMagnitude)
        
        // Verificar se calibração está completa
        val elapsedMs = timestampMs - calibrationStartTimeMs
        if (elapsedMs >= calibrationDurationMs && gyroSamples.size >= minSamplesForCalibration) {
            finalize()
        }
    }
    
    /**
     * Finaliza a calibração e calcula os thresholds
     */
    private fun finalize() {
        if (gyroSamples.isEmpty()) {
            Log.w(TAG, "Calibração sem amostras, usando valores default")
            isCalibrated = true
            return
        }
        
        // Calcular energia média do giroscópio (baseline em repouso)
        val gyroEnergy = gyroSamples.fold(0.0) { acc, x -> acc + x * x }
        baselineEnergy = gyroEnergy / gyroSamples.size
        
        // Calcular thresholds dinâmicos
        val baselineRms = kotlin.math.sqrt(baselineEnergy)
        
        movementThreshold = (baselineRms * MOVEMENT_THRESHOLD_MULTIPLIER)
            .coerceIn(MIN_MOVEMENT_THRESHOLD, MAX_MOVEMENT_THRESHOLD)
        
        stabilityThreshold = (baselineRms * STABILITY_THRESHOLD_MULTIPLIER)
            .coerceIn(MIN_STABILITY_THRESHOLD, MAX_STABILITY_THRESHOLD)
        
        isCalibrated = true
        
        Log.d(TAG, """
            |Calibração concluída:
            |  - Amostras: ${gyroSamples.size}
            |  - Baseline RMS: ${"%.4f".format(baselineRms)}
            |  - Movement threshold: ${"%.4f".format(movementThreshold)}
            |  - Stability threshold: ${"%.4f".format(stabilityThreshold)}
        """.trimMargin())
    }
    
    /**
     * Força finalização da calibração (usar se precisar começar antes do tempo)
     */
    fun forceFinalize() {
        if (!isCalibrated) {
            finalize()
        }
    }
    
    /**
     * Reinicia a calibração para nova sessão
     */
    fun reset() {
        isCalibrated = false
        baselineEnergy = 0.0
        movementThreshold = DEFAULT_MOVEMENT_THRESHOLD
        stabilityThreshold = DEFAULT_STABILITY_THRESHOLD
        gyroSamples.clear()
        accelSamples.clear()
        calibrationStartTimeMs = 0L
        Log.d(TAG, "Calibrador reiniciado")
    }
    
    /**
     * Retorna o progresso da calibração (0.0 a 1.0)
     */
    fun calibrationProgress(currentTimeMs: Long): Double {
        if (isCalibrated) return 1.0
        if (calibrationStartTimeMs == 0L) return 0.0
        val elapsed = currentTimeMs - calibrationStartTimeMs
        return (elapsed.toDouble() / calibrationDurationMs).coerceIn(0.0, 1.0)
    }
}
