package com.ufpr.equilibrium.feature_teste

/**
 * Estados possíveis do ciclo SLS (Sentar-Levantar-Sentar)
 * 
 * Transições válidas:
 * SENTADO → LEVANTANDO (detecta movimento ascendente)
 * LEVANTANDO → EM_PE (detecta estabilidade em pé)
 * EM_PE → SENTANDO (detecta movimento descendente)
 * SENTANDO → SENTADO (detecta estabilidade sentado) [+1 ciclo completo]
 * 
 * Transições inválidas são bloqueadas pela FSM:
 * SENTADO → EM_PE (não pode pular LEVANTANDO)
 * SENTADO → SENTANDO (não pode sentar sem ter levantado)
 * EM_PE → LEVANTANDO (não pode levantar de novo sem sentar)
 * etc.
 */
enum class SlsState {
    /**
     * Estado inicial e final de cada ciclo
     * Usuário está sentado de forma estável
     */
    SENTADO,
    
    /**
     * Transição ascendente detectada
     * Giroscópio indica rotação correspondente a levantar
     */
    LEVANTANDO,
    
    /**
     * Postura ereta estável
     * Baixa energia de movimento + orientação vertical confirmada
     */
    EM_PE,
    
    /**
     * Transição descendente detectada
     * Giroscópio indica rotação correspondente a sentar
     */
    SENTANDO
}

/**
 * Direção do movimento detectado
 */
enum class MovementDirection {
    UP,   // Movimento de levantar
    DOWN  // Movimento de sentar
}

/**
 * Eventos que podem causar transições na FSM
 */
sealed class SlsEvent {
    /**
     * Movimento significativo detectado em uma direção
     */
    data class MovementStart(val direction: MovementDirection) : SlsEvent()
    
    /**
     * Estabilidade alcançada (baixa energia de movimento)
     */
    object StabilityReached : SlsEvent()
}
