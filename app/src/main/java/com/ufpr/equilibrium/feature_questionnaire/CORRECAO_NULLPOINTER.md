# 🔧 Correção: NullPointerException no Envio do Questionário

## 🐛 Problema Identificado

**Erro:**
```
java.lang.NullPointerException: Parameter specified as non-null is null: 
method kotlin.collections.CollectionsKt___CollectionsKt.sortedWith, 
parameter <this>
```

**Localização:** `QuestionnaireViewModel.kt:282`

**Causa:** 
O campo `questions` na resposta da API estava retornando `null`, mas o código esperava uma lista não-nula, causando crash ao tentar fazer `sortedBy()`.

## ✅ Soluções Implementadas

### 1. **QuestionnaireStructureResponse.kt**

#### Antes:
```kotlin
data class QuestionnaireStructureResponse(
    val questions: List<QuestionDto>  // ❌ Non-null
)

data class QuestionDto(
    val options: List<OptionDto>  // ❌ Non-null
)
```

#### Depois:
```kotlin
data class QuestionnaireStructureResponse(
    val questions: List<QuestionDto>?  // ✅ Nullable
)

data class QuestionDto(
    val options: List<OptionDto>?  // ✅ Nullable
)
```

### 2. **QuestionnaireViewModel.kt**

#### Adicionada validação:
```kotlin
onSuccess = { structure ->
    // ✅ Validate structure
    if (structure.questions == null || structure.questions.isEmpty()) {
        _uiState.update { it.copy(isLoading = false, error = "Estrutura do questionário inválida") }
        onError("Estrutura do questionário não contém questões")
        return@getIVCF20QuestionnaireStructure
    }
    
    // Store questionnaire ID
    apiQuestionnaireId = structure.id
    
    // ✅ Safe navigation
    structure.questions.sortedBy { it.order }.forEachIndexed { index, questionDto ->
        questionIdMapping[index] = questionDto.id
        
        // ✅ Safe navigation with Elvis operator
        val optionMap = questionDto.options?.mapIndexed { optIdx, optionDto ->
            optIdx to optionDto.id
        }?.toMap() ?: emptyMap()
        optionIdMapping[index] = optionMap
    }
}
```

### 3. **QuestionnaireMapper.kt**

#### Todos os métodos agora usam safe navigation:

```kotlin
fun mapToLocalQuestions(response: QuestionnaireStructureResponse): List<Question> {
    return response.questions
        ?.sortedBy { it.order }
        ?.mapIndexed { index, questionDto ->
            mapQuestionDtoToLocal(questionDto, index)
        } ?: emptyList()  // ✅ Retorna lista vazia se null
}

fun mapQuestionDtoToLocal(dto: QuestionDto, localId: Int): Question {
    return Question(
        options = dto.options?.map { mapOptionDtoToLocal(it) } ?: emptyList()  // ✅ Safe
    )
}

fun createIdMapping(response: QuestionnaireStructureResponse): Map<Int, String> {
    return response.questions
        ?.sortedBy { it.order }
        ?.mapIndexed { index, questionDto -> index to questionDto.id }
        ?.toMap() ?: emptyMap()  // ✅ Safe
}
```

## 🛡️ Proteções Adicionadas

### ✅ Validações:
1. **Validação de null**: Verifica se `questions` não é null
2. **Validação de vazio**: Verifica se `questions` não está vazio
3. **Elvis operator**: Retorna valores padrão (emptyList, emptyMap) se null
4. **Safe call operator**: Usa `?.` em todas as operações de lista

### ✅ Mensagens de Erro:
- "Estrutura do questionário inválida" - usuário
- "Estrutura do questionário não contém questões" - callback de erro

### ✅ Tratamento Gracioso:
Em vez de crash, agora:
1. Mostra erro ao usuário
2. Desabilita loading
3. Permite retry ou cancelamento

## 📊 Fluxo de Erro Corrigido

```
API retorna structure com questions = null
         ↓
✅ Validação detecta null
         ↓
✅ Atualiza UI state com erro
         ↓
✅ Chama onError callback
         ↓
✅ Activity mostra dialog de erro
         ↓
Usuário pode tentar novamente ou cancelar
```

## 🧪 Cenários Testáveis

### Cenário 1: API retorna questions = null
- ✅ Não crasha
- ✅ Mostra erro "Estrutura do questionário não contém questões"
- ✅ Permite retry

### Cenário 2: API retorna questions = []
- ✅ Não crasha
- ✅ Mostra erro "Estrutura do questionário não contém questões"
- ✅ Permite retry

### Cenário 3: QuestionDto com options = null
- ✅ Não crasha
- ✅ Questão criada com options = emptyList()
- ✅ Continua processamento

### Cenário 4: API retorna dados válidos
- ✅ Funciona normalmente
- ✅ Cria mapeamentos
- ✅ Envia respostas

## 📝 Arquivos Modificados

1. ✅ `QuestionnaireStructureResponse.kt` - Campos nullable
2. ✅ `QuestionnaireViewModel.kt` - Validações e safe navigation
3. ✅ `QuestionnaireMapper.kt` - Safe navigation em todos métodos

## 🔍 Possíveis Causas do Null na API

1. **Questionário não cadastrado no backend**
   - Endpoint retorna estrutura vazia ou null

2. **Erro de serialização**
   - Campo com nome diferente no JSON

3. **Versão incompatível da API**
   - Backend retornando formato antigo

4. **Permissões de acesso**
   - Token sem permissão para ver questões

## 💡 Recomendações

### Imediato:
- [ ] Verificar se endpoint `/questionnaires/ivcf-20` está configurado no backend
- [ ] Verificar formato do JSON retornado pela API
- [ ] Confirmar que token tem permissões corretas

### Futuro:
- [ ] Adicionar logs detalhados da resposta da API
- [ ] Implementar retry automático com backoff exponencial
- [ ] Cachear estrutura do questionário localmente
- [ ] Adicionar telemetria para rastrear erros de API

---

**Data da correção**: 2025-12-18  
**Tipo de erro**: NullPointerException  
**Status**: ✅ Corrigido com null safety
