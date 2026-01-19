package com.ufpr.equilibrium.feature_teste

import android.util.Log

/**
 * Máquina de Estados Finita para detecção de ciclos SLS
 * 
 * Responsabilidades:
 * - Gerenciar transições entre estados válidos
 * - Bloquear transições biologicamente inválidas
 * - Aplicar debounce para evitar transições espúrias
 * - Contar ciclos completos (SENTADO → ... → SENTADO)
 */
class SlsStateMachine {
    
    companion object {
        private const val TAG = "SlsStateMachine"
        
        // Tempo mínimo entre transições (ms) para evitar ruído
        private const val DEBOUNCE_MS = 150L
        
        // Tempo mínimo em estado de movimento antes de aceitar estabilidade
        private const val MIN_MOVEMENT_DURATION_MS = 200L
    }
    
    /**
     * Estado atual da máquina
     */
    var currentState: SlsState = SlsState.SENTADO
        private set
    
    /**
     * Contador de ciclos completos
     * Um ciclo é completo quando retorna ao estado SENTADO após passar por EM_PE
     */
    var cycleCount: Int = 0
        private set
    
    // Timestamps para debouncing e validação
    private var lastTransitionTimeMs: Long = 0L
    private var stateEntryTimeMs: Long = System.currentTimeMillis()
    
    /**
     * Processa um evento e atualiza o estado da máquina
     * 
     * @param event evento detectado (movimento ou estabilidade)
     * @param timestampMs timestamp atual em milissegundos
     * @return true se houve transição de estado
     */
    fun processEvent(event: SlsEvent, timestampMs: Long): Boolean {
        // Aplicar debounce
        if (timestampMs - lastTransitionTimeMs < DEBOUNCE_MS) {
            return false
        }
        
        val previousState = currentState
        val nextState = computeNextState(event, timestampMs)
        
        if (nextState != null && nextState != currentState) {
            Log.d(TAG, "Transição: $currentState → $nextState (evento: $event)")
            
            currentState = nextState
            lastTransitionTimeMs = timestampMs
            stateEntryTimeMs = timestampMs
            
            // Verificar se completou um ciclo
            if (previousState == SlsState.SENTANDO && nextState == SlsState.SENTADO) {
                cycleCount++
                Log.d(TAG, "✓ Ciclo completo! Total: $cycleCount")
            }
            
            return true
        }
        
        return false
    }
    
    /**
     * Computa o próximo estado baseado no evento e estado atual
     * Retorna null se a transição for inválida
     */
    private fun computeNextState(event: SlsEvent, timestampMs: Long): SlsState? {
        return when (currentState) {
            SlsState.SENTADO -> {
                // De SENTADO só pode ir para LEVANTANDO se detectar movimento UP
                when (event) {
                    is SlsEvent.MovementStart -> {
                        if (event.direction == MovementDirection.UP) {
                            SlsState.LEVANTANDO
                        } else {
                            null // Ignorar movimento DOWN quando já sentado
                        }
                    }
                    is SlsEvent.StabilityReached -> null // Já está estável
                }
            }
            
            SlsState.LEVANTANDO -> {
                when (event) {
                    is SlsEvent.StabilityReached -> {
                        // Só aceita estabilidade se passou tempo mínimo em movimento
                        val durationInState = timestampMs - stateEntryTimeMs
                        if (durationInState >= MIN_MOVEMENT_DURATION_MS) {
                            SlsState.EM_PE
                        } else {
                            null
                        }
                    }
                    is SlsEvent.MovementStart -> {
                        // Se detectar movimento DOWN enquanto levantando, 
                        // pode ser um movimento abortado - voltar para SENTADO
                        if (event.direction == MovementDirection.DOWN) {
                            Log.d(TAG, "Movimento abortado durante levantamento")
                            SlsState.SENTADO
                        } else {
                            null // Continuar levantando
                        }
                    }
                }
            }
            
            SlsState.EM_PE -> {
                when (event) {
                    is SlsEvent.MovementStart -> {
                        if (event.direction == MovementDirection.DOWN) {
                            SlsState.SENTANDO
                        } else {
                            null // Ignorar movimento UP quando já em pé
                        }
                    }
                    is SlsEvent.StabilityReached -> null // Já está estável
                }
            }
            
            SlsState.SENTANDO -> {
                when (event) {
                    is SlsEvent.StabilityReached -> {
                        val durationInState = timestampMs - stateEntryTimeMs
                        if (durationInState >= MIN_MOVEMENT_DURATION_MS) {
                            SlsState.SENTADO // Ciclo completo!
                        } else {
                            null
                        }
                    }
                    is SlsEvent.MovementStart -> {
                        // Se detectar movimento UP enquanto sentando,
                        // pode ser mudança de direção - voltar para EM_PE
                        if (event.direction == MovementDirection.UP) {
                            Log.d(TAG, "Movimento revertido durante sentamento")
                            SlsState.EM_PE
                        } else {
                            null // Continuar sentando
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Reinicia a máquina de estados para estado inicial
     */
    fun reset() {
        currentState = SlsState.SENTADO
        cycleCount = 0
        lastTransitionTimeMs = 0L
        stateEntryTimeMs = System.currentTimeMillis()
        Log.d(TAG, "FSM reiniciada")
    }
    
    /**
     * Retorna tempo em milissegundos desde a última transição
     */
    fun timeSinceLastTransition(currentTimeMs: Long): Long {
        return currentTimeMs - lastTransitionTimeMs
    }
}
