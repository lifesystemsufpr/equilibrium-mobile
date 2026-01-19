package com.ufpr.equilibrium.feature_teste

/**
 * Buffer circular para janela deslizante de dados de sensores
 * Mantém as últimas N amostras para análise de features
 * 
 * @param size tamanho máximo da janela
 *             - 7 amostras a 25Hz = ~280ms (recomendado para SLS)
 */
class SlidingWindow(private val size: Int = 7) {
    
    private val buffer = ArrayDeque<Double>(size)
    
    /**
     * Adiciona um novo valor à janela
     * Remove automaticamente o valor mais antigo se a janela estiver cheia
     */
    fun add(value: Double) {
        if (buffer.size >= size) {
            buffer.removeFirst()
        }
        buffer.addLast(value)
    }
    
    /**
     * Verifica se a janela tem amostras suficientes para análise
     */
    fun isFull(): Boolean = buffer.size >= size
    
    /**
     * Retorna os valores atuais da janela como lista
     */
    fun toList(): List<Double> = buffer.toList()
    
    /**
     * Retorna o número atual de amostras na janela
     */
    fun count(): Int = buffer.size
    
    /**
     * Limpa todos os dados da janela
     */
    fun clear() {
        buffer.clear()
    }
    
    /**
     * Aplica uma operação de redução sobre todos os valores
     */
    fun <R> fold(initial: R, operation: (acc: R, Double) -> R): R {
        return buffer.fold(initial, operation)
    }
}
