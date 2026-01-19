package com.ufpr.equilibrium.feature_teste

import android.util.Log
import kotlin.math.abs

/**
 * Detector de ciclos SLS baseado em análise de picos
 * Evita uso de thresholds fixos, usando padrão de picos e vales
 * 
 * Princípio:
 * - Ciclo = Pico Positivo → Vale Negativo → Pico Positivo → Vale Negativo
 * - Cada par (pico positivo + vale negativo) = meio ciclo
 * - 2 meios ciclos = 1 ciclo completo
 */
class SlsPeakDetector (
    private val onCycleComplete: (count: Int) -> Unit = {}
) {
    
    companion object {
        private const val TAG = "SlsPeakDetector"
        
        // Threshold mínimo apenas para filtrar ruído muito pequeno (rad/s)
        // Baseado em logs reais: valores válidos ficam em ~0.2-0.3 rad/s
        private const val MIN_PEAK_MAGNITUDE = 0.15
        
        // Janela para detectar se é realmente um pico (amostras)
        // 60Hz -> 6 samples ~= 100ms
        private const val PEAK_WINDOW = 6
        
        // Tempo mínimo entre picos do mesmo tipo (ms) - evita detecção duplicada
        private const val MIN_TIME_BETWEEN_SAME_PEAKS = 300L
    }
    
    private var cycleCount = 0
    
    // Buffer circular para análise de picos
    private val gyroBuffer = ArrayDeque<Double>(PEAK_WINDOW * 2 + 1)
    private val timeBuffer = ArrayDeque<Long>(PEAK_WINDOW * 2 + 1)
    
    // Histórico de picos detectados
    private var lastPositivePeakTime = 0L
    private var lastNegativePeakTime = 0L
    
    // Contador de meios-ciclos
    private var halfCycles = 0
    
    // Estado: esperando pico positivo (true) ou negativo (false)
    private var expectingPositivePeak = true
    
    /**
     * Processa novo valor do giroscópio
     */
    fun processSample(gyroValue: Double, timestampMs: Long) {
        // Adicionar ao buffer
        gyroBuffer.addLast(gyroValue)
        timeBuffer.addLast(timestampMs)
        
        // Manter tamanho do buffer
        val maxSize = PEAK_WINDOW * 2 + 1
        while (gyroBuffer.size > maxSize) {
            gyroBuffer.removeFirst()
            timeBuffer.removeFirst()
        }
        
        // Precisa de buffer completo para análise
        if (gyroBuffer.size < maxSize) {
            if (gyroBuffer.size % 5 == 0) {  // Log a cada 5 amostras
                Log.d(TAG, "Preenchendo buffer: ${gyroBuffer.size}/$maxSize")
            }
            return
        }
        
        // Analisar se o ponto central é um pico
        val centerIndex = PEAK_WINDOW
        val centerValue = gyroBuffer[centerIndex]
        val centerTime = timeBuffer[centerIndex]
        
        // LOG: Mostrar valores recebidos periodicamente
        if (timestampMs % 1000 < 20) {  // A cada ~1 segundo
            Log.d(TAG, "Gyro atual: %.3f rad/s (threshold: %.3f)".format(centerValue, MIN_PEAK_MAGNITUDE))
        }
        
        // Verificar magnitude mínima
        if (abs(centerValue) < MIN_PEAK_MAGNITUDE) return
        
        
        // LOG: Valor passou do threshold mas não é pico local
        if (centerValue > 0 && !isPeak(centerIndex, isPositive = true)) {
            Log.d(TAG, "⚠️ Valor positivo %.3f passou threshold mas NÃO é pico local".format(centerValue))
        }
        if (centerValue < 0 && !isPeak(centerIndex, isPositive = false)) {
            Log.d(TAG, "⚠️ Valor negativo %.3f passou threshold mas NÃO é pico local".format(centerValue))
        }
        
        // Detectar pico positivo (máximo local)
        if (centerValue > 0 && isPeak(centerIndex, isPositive = true)) {
            // Evitar detecção duplicada
            if (centerTime - lastPositivePeakTime > MIN_TIME_BETWEEN_SAME_PEAKS) {
                onPositivePeak(centerValue, centerTime)
                lastPositivePeakTime = centerTime
            } else {
                Log.d(TAG, "🔺 Pico POSITIVO ignorado (muito próximo do anterior: ${centerTime - lastPositivePeakTime}ms)")
            }
        }
        
        // Detectar pico negativo (mínimo local)
        if (centerValue < 0 && isPeak(centerIndex, isPositive = false)) {
            // Evitar detecção duplicada
            if (centerTime - lastNegativePeakTime > MIN_TIME_BETWEEN_SAME_PEAKS) {
                onNegativePeak(centerValue, centerTime)
                lastNegativePeakTime = centerTime
            } else {
                Log.d(TAG, "🔻 Pico NEGATIVO ignorado (muito próximo do anterior: ${centerTime - lastNegativePeakTime}ms)")
            }
        }
    }
    
    /**
     * Verifica se o ponto no índice é um pico (máximo ou mínimo local)
     */
    private fun isPeak(index: Int, isPositive: Boolean): Boolean {
        val value = gyroBuffer[index]
        
        // Para pico positivo: deve ser maior que vizinhos
        // Para pico negativo: deve ser menor que vizinhos
        for (i in 0 until PEAK_WINDOW) {
            val before = index - i - 1
            val after = index + i + 1
            
            if (isPositive) {
                // Máximo local
                if (gyroBuffer[before] >= value || gyroBuffer[after] >= value) {
                    return false
                }
            } else {
                // Mínimo local
                if (gyroBuffer[before] <= value || gyroBuffer[after] <= value) {
                    return false
                }
            }
        }
        
        return true
    }
    
    /**
     * Callback quando pico positivo é detectado
     */
    private fun onPositivePeak(value: Double, time: Long) {
        Log.d(TAG, "🔺 Pico POSITIVO: %.3f rad/s (esperado: $expectingPositivePeak)".format(value))
        
        if (expectingPositivePeak) {
            halfCycles++
            expectingPositivePeak = false
            
            // Um ciclo completo SLS = 4 transições (2 picos + 2 vales)
            // Sentado → 🔺Levantar → 🔻Sentar → 🔺Levantar → 🔻Sentar = 1 ciclo
            if (halfCycles >= 4) {
                cycleCount++
                halfCycles = 0
                Log.d(TAG, "✅ CICLO $cycleCount COMPLETO!")
                onCycleComplete(cycleCount)
            }
        }
    }
    
    /**
     * Callback quando pico negativo é detectado
     */
    private fun onNegativePeak(value: Double, time: Long) {
        Log.d(TAG, "🔻 Pico NEGATIVO: %.3f rad/s (esperado: ${!expectingPositivePeak})".format(value))
        
        if (!expectingPositivePeak) {
            halfCycles++
            expectingPositivePeak = true
            
            // Um ciclo completo SLS = 4 transições
            if (halfCycles >= 4) {
                cycleCount++
                halfCycles = 0
                Log.d(TAG, "✅ CICLO $cycleCount COMPLETO!")
                onCycleComplete(cycleCount)
            }
        }
    }
    
    /**
     * Reseta o detector
     */
    fun reset() {
        cycleCount = 0
        halfCycles = 0
        gyroBuffer.clear()
        timeBuffer.clear()
        lastPositivePeakTime = 0L
        lastNegativePeakTime = 0L
        expectingPositivePeak = true
        Log.d(TAG, "Detector resetado")
    }
    
    /**
     * Retorna contador atual
     */
    fun getCycleCount(): Int = cycleCount
    
    /**
     * Imprime resumo final
     */
    fun printFinalSummary() {
        println("======================================")
        println("       RESULTADO FINAL SLS           ")
        println("======================================")
        println("  Total de ciclos completos: $cycleCount")
        println("  Meios-ciclos pendentes: $halfCycles")
        println("======================================")
        
        Log.i(TAG, "RESULTADO FINAL: $cycleCount ciclos SLS completados")
    }
}
