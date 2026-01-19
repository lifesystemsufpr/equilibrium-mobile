package com.ufpr.equilibrium.feature_teste

import android.util.Log

/**
 * Detector principal de ciclos SLS (Sentar-Levantar-Sentar)
 * 
 * Integra todos os componentes do pipeline:
 * Sensores → Filtros EMA → Janela Deslizante → Features → Fusão → FSM → Contador
 * 
 * @param onCycleComplete callback chamado quando um ciclo completo é detectado
 * @param onStateChange callback opcional chamado a cada mudança de estado
 */
class SlsCycleDetector(
    private val onCycleComplete: (count: Int) -> Unit = {},
    private val onStateChange: (state: SlsState) -> Unit = {}
) {
    
    companion object {
        private const val TAG = "SlsCycleDetector"
        
        // Período mínimo de estabilidade para confirmar estado estático (ms)
        private const val STABILITY_DURATION_MS = 150L
        
        // Thresholds de direção (rad/s) - valores mais baixos para maior sensibilidade
        private const val DIRECTION_UP_THRESHOLD = 0.15
        private const val DIRECTION_DOWN_THRESHOLD = -0.15
        
        // Ativar logging detalhado para debug
        private const val DEBUG_LOGGING = true
    }  
    
    // ========== Componentes internos ==========
    
    // Filtros EMA para suavização (um por eixo)
    private val gyroFilterX = EMAFilter(alpha = 0.3)
    private val gyroFilterY = EMAFilter(alpha = 0.3)
    private val gyroFilterZ = EMAFilter(alpha = 0.3)
    
    private val accelFilterX = EMAFilter(alpha = 0.4)
    private val accelFilterY = EMAFilter(alpha = 0.4)
    private val accelFilterZ = EMAFilter(alpha = 0.4)
    
    // Janelas deslizantes para análise de features
    private val gyroWindowMagnitude = SlidingWindow(size = 5)  // ~200ms - mais rápido
    private val accelWindowMagnitude = SlidingWindow(size = 5)
    
    // Para detectar direção do movimento - usar múltiplos eixos
    private val gyroPitchWindow = SlidingWindow(size = 3)  // Mais curto para resposta rápida
    private val gyroYawWindow = SlidingWindow(size = 3)    // Eixo Y também
    
    // Máquina de estados
    private val stateMachine = SlsStateMachine()
    
    // Calibrador para thresholds adaptativos
    private val calibrator = SlsCalibrator(calibrationDurationMs = 2000L)
    
    // Estado de estabilidade
    private var stabilityStartTimeMs: Long = 0L
    private var wasStable: Boolean = false
    private var lastProcessedTimeMs: Long = 0L
    
    // ========== Propriedades públicas ==========
    
    /**
     * Contador atual de ciclos completos
     */
    val cycleCount: Int get() = stateMachine.cycleCount
    
    /**
     * Estado atual da FSM
     */
    val currentState: SlsState get() = stateMachine.currentState
    
    /**
     * Indica se ainda está em fase de calibração
     */
    fun isCalibrating(): Boolean = !calibrator.isCalibrated
    
    /**
     * Progresso da calibração (0.0 a 1.0)
     */
    fun calibrationProgress(): Double = calibrator.calibrationProgress(System.currentTimeMillis())
    
    // ========== Processamento ==========
    
    /**
     * Processa uma nova amostra de sensores
     * Deve ser chamado a cada evento de sensor (~25Hz)
     * 
     * @param gyroX velocidade angular X (rad/s)
     * @param gyroY velocidade angular Y (rad/s)
     * @param gyroZ velocidade angular Z (rad/s)
     * @param accelX aceleração X (m/s²)
     * @param accelY aceleração Y (m/s²)
     * @param accelZ aceleração Z (m/s²)
     * @param timestampMs timestamp em milissegundos
     */
    fun processSensorData(
        gyroX: Double, gyroY: Double, gyroZ: Double,
        accelX: Double, accelY: Double, accelZ: Double,
        timestampMs: Long
    ) {
        lastProcessedTimeMs = timestampMs
        
        // 1. Aplicar filtros EMA
        val filteredGyroX = gyroFilterX.filter(gyroX)
        val filteredGyroY = gyroFilterY.filter(gyroY)
        val filteredGyroZ = gyroFilterZ.filter(gyroZ)
        
        val filteredAccelX = accelFilterX.filter(accelX)
        val filteredAccelY = accelFilterY.filter(accelY)
        val filteredAccelZ = accelFilterZ.filter(accelZ)
        
        // 2. Calcular magnitudes
        val gyroMag = FeatureExtractor.magnitude3D(filteredGyroX, filteredGyroY, filteredGyroZ)
        val accelMag = FeatureExtractor.magnitude3D(filteredAccelX, filteredAccelY, filteredAccelZ)
        
        // 3. Adicionar às janelas deslizantes
        gyroWindowMagnitude.add(gyroMag)
        accelWindowMagnitude.add(accelMag)
        gyroPitchWindow.add(filteredGyroX)  // Pitch é rotação em X
        gyroYawWindow.add(filteredGyroY)    // Yaw é rotação em Y
        
        // Debug logging periódico
        if (DEBUG_LOGGING && timestampMs % 500 < 50) {
            Log.d(TAG, "Gyro mag: ${"%.3f".format(gyroMag)}, X: ${"%.3f".format(filteredGyroX)}, Y: ${"%.3f".format(filteredGyroY)}")
        }
        
        // 4. Calibração (primeiros 2 segundos)
        if (!calibrator.isCalibrated) {
            calibrator.addSample(gyroMag, accelMag, timestampMs)
            return  // Não processar FSM durante calibração
        }
        
        // 5. Verificar se janelas têm dados suficientes
        if (!gyroWindowMagnitude.isFull()) {
            return
        }
        
        // 6. Extrair features
        val gyroFeatures = FeatureExtractor.extract(gyroWindowMagnitude.toList())
        val pitchValues = gyroPitchWindow.toList()
        val yawValues = gyroYawWindow.toList()
        val pitchMean = if (pitchValues.isNotEmpty()) pitchValues.average() else 0.0
        val yawMean = if (yawValues.isNotEmpty()) yawValues.average() else 0.0
        
        // 7. Análise de movimento/estabilidade baseada em fusão de sensores
        val event = analyzeMovement(gyroFeatures, pitchMean, yawMean, timestampMs)
        
        // 8. Processar evento na FSM
        if (event != null) {
            val previousCount = stateMachine.cycleCount
            val previousState = stateMachine.currentState
            
            val transitioned = stateMachine.processEvent(event, timestampMs)
            
            if (transitioned) {
                onStateChange(stateMachine.currentState)
                
                // Verificar se houve novo ciclo
                if (stateMachine.cycleCount > previousCount) {
                    Log.d(TAG, "🎯 Ciclo ${stateMachine.cycleCount} detectado!")
                    onCycleComplete(stateMachine.cycleCount)
                }
            }
        }
    }
    
    /**
     * Analisa movimento usando fusão de sensores e decide qual evento gerar
     */
    private fun analyzeMovement(
        gyroFeatures: SignalFeatures,
        pitchMean: Double,
        yawMean: Double,
        timestampMs: Long
    ): SlsEvent? {
        val gyroRms = gyroFeatures.rms
        
        // Detectar se está em movimento ou estável
        val isMoving = gyroRms > calibrator.movementThreshold
        val isStable = gyroRms < calibrator.stabilityThreshold
        
        // Lógica de detecção de direção usando múltiplos eixos
        // Combina pitch (X) e yaw (Y) para melhor detecção
        val combinedUp = pitchMean + yawMean
        val combinedDown = pitchMean + yawMean
        
        // Thresholds mais baixos e fórmula combinada
        val movingUp = combinedUp > DIRECTION_UP_THRESHOLD || pitchMean > DIRECTION_UP_THRESHOLD
        val movingDown = combinedDown < DIRECTION_DOWN_THRESHOLD || pitchMean < DIRECTION_DOWN_THRESHOLD
        
        // Debug logging para cada avaliação
        if (DEBUG_LOGGING && isMoving) {
            Log.d(TAG, "MOVIMENTO: rms=${"%.3f".format(gyroRms)} pitch=${"%.3f".format(pitchMean)} yaw=${"%.3f".format(yawMean)} up=$movingUp down=$movingDown state=${stateMachine.currentState}")
        }
        
        // Atualizar rastreamento de estabilidade
        if (isStable) {
            if (!wasStable) {
                stabilityStartTimeMs = timestampMs
                wasStable = true
            }
            val stableDuration = timestampMs - stabilityStartTimeMs
            
            // Confirmar estabilidade após período mínimo
            if (stableDuration >= STABILITY_DURATION_MS) {
                if (DEBUG_LOGGING) {
                    Log.d(TAG, "ESTABILIDADE atingida após ${stableDuration}ms")
                }
                return SlsEvent.StabilityReached
            }
        } else {
            wasStable = false
        }
        
        // Detectar início de movimento com direção
        if (isMoving) {
            // Primeiro tentar detectar direção clara
            if (movingUp && !movingDown) {
                return SlsEvent.MovementStart(MovementDirection.UP)
            }
            if (movingDown && !movingUp) {
                return SlsEvent.MovementStart(MovementDirection.DOWN)
            }
            
            // Se movimento significativo mas direção ambígua,
            // inferir baseado no estado atual da FSM
            if (gyroRms > calibrator.movementThreshold * 1.5) {
                return when (stateMachine.currentState) {
                    SlsState.SENTADO -> SlsEvent.MovementStart(MovementDirection.UP)
                    SlsState.EM_PE -> SlsEvent.MovementStart(MovementDirection.DOWN)
                    else -> null
                }
            }
        }
        
        return null
    }
    
    /**
     * Reinicia o detector para nova sessão
     */
    fun reset() {
        // Reiniciar filtros
        gyroFilterX.reset()
        gyroFilterY.reset()
        gyroFilterZ.reset()
        accelFilterX.reset()
        accelFilterY.reset()
        accelFilterZ.reset()
        
        // Reiniciar janelas
        gyroWindowMagnitude.clear()
        accelWindowMagnitude.clear()
        gyroPitchWindow.clear()
        gyroYawWindow.clear()
        
        // Reiniciar FSM e calibrador
        stateMachine.reset()
        calibrator.reset()
        
        // Reiniciar estado
        stabilityStartTimeMs = 0L
        wasStable = false
        lastProcessedTimeMs = 0L
        
        Log.d(TAG, "Detector SLS reiniciado")
    }
    
    /**
     * Retorna estatísticas para debug
     */
    fun getDebugStats(): String {
        return """
            |SLS Detector Stats:
            |  Estado: ${stateMachine.currentState}
            |  Ciclos: ${stateMachine.cycleCount}
            |  Calibrado: ${calibrator.isCalibrated}
            |  Movement Threshold: ${"%.4f".format(calibrator.movementThreshold)}
            |  Stability Threshold: ${"%.4f".format(calibrator.stabilityThreshold)}
        """.trimMargin()
    }
    
    /**
     * Imprime resumo final (para uso ao término do teste)
     */
    fun printFinalSummary() {
        println("======================================")
        println("       RESULTADO FINAL SLS           ")
        println("======================================")
        println("  Total de ciclos completos: ${stateMachine.cycleCount}")
        println("  Estado final: ${stateMachine.currentState}")
        println("======================================")
        
        Log.i(TAG, "RESULTADO FINAL: ${stateMachine.cycleCount} ciclos SLS completados")
    }
}
