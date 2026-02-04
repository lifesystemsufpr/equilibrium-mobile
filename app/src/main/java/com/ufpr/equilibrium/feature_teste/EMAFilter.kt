package com.ufpr.equilibrium.feature_teste

/**
 * Filtro de Média Móvel Exponencial (EMA)
 * Suaviza sinais de sensores removendo ruído de alta frequência
 * 
 * @param alpha fator de suavização (0.0 a 1.0)
 *              - valores menores = mais suave, maior lag
 *              - valores maiores = menos suave, mais responsivo
 *              - recomendado: 0.2-0.4 para sensores de movimento
 */
class EMAFilter(private val alpha: Double = 0.3) {
    
    private var value: Double? = null
    
    /**
     * Aplica o filtro EMA a um novo valor de entrada
     * Fórmula: EMA_t = α * x_t + (1 - α) * EMA_{t-1}
     * 
     * @param input valor bruto do sensor
     * @return valor filtrado
     */
    fun filter(input: Double): Double {
        val current = value
        return if (current == null) {
            value = input
            input
        } else {
            val filtered = alpha * input + (1 - alpha) * current
            value = filtered
            filtered
        }
    }
    
    /**
     * Reinicia o filtro para estado inicial
     * ]
     * Usar quando iniciar nova sessão de coleta
     */
    fun reset() {
        value = null
    }
    
    /**
     * Retorna o último valor filtrado ou null se não houver histórico
     */
    fun currentValue(): Double? = value
}
