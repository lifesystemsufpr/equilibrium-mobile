# ✅ Verificação do Payload POST /questionnaires/response

## 📊 Comparação: Implementado vs API Schema

### ✅ Campos Principais (QuestionnaireResponseRequest)

| Campo | Tipo | Implementado | API Schema | Status |
|-------|------|--------------|------------|--------|
| `participantId` | String | ✅ | ✅ | ✅ OK |
| `healthProfessionalId` | String | ✅ | ✅ | ✅ OK |
| `questionnaireId` | String | ✅ | ✅ | ✅ OK |
| `answers` | List | ✅ | ✅ | ✅ OK |

### ✅ Campos de Answer (AnswerRequest)

| Campo | Tipo | Implementado | API Schema | Status |
|-------|------|--------------|------------|--------|
| `questionId` | String | ✅ | ✅ | ✅ OK |
| `selectedOptionId` | String | ✅ | ✅ | ✅ OK |
| `valueText` | String? | ✅ | ✅ | ✅ OK (Adicionado) |

## 📝 Estrutura Completa Implementada

```kotlin
data class QuestionnaireResponseRequest(
    @SerializedName("participantId")
    val participantId: String,              // ✅ Obrigatório
    
    @SerializedName("healthProfessionalId")
    val healthProfessionalId: String,       // ✅ Obrigatório
    
    @SerializedName("questionnaireId")
    val questionnaireId: String,            // ✅ Obrigatório
    
    @SerializedName("answers")
    val answers: List<AnswerRequest>        // ✅ Obrigatório
)

data class AnswerRequest(
    @SerializedName("questionId")
    val questionId: String,                 // ✅ Obrigatório
    
    @SerializedName("selectedOptionId")
    val selectedOptionId: String,           // ✅ Obrigatório
    
    @SerializedName("valueText")
    val valueText: String? = null          // ✅ Opcional
)
```

## 📋 Exemplo de JSON Gerado

```json
{
  "participantId": "0a775d40-65c3-4514-ad1e-d31f023a2191",
  "healthProfessionalId": "18b0b378-1060-42d0-8d82-4a11ba7d2cee",
  "questionnaireId": "9825800d-6ec8-4220-ad50-eeb10a84c337",
  "answers": [
    {
      "questionId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "selectedOptionId": "a3c5b8f2-91ca-4a7d-9f4e-2d8e6b1c7a9d",
      "valueText": null
    },
    {
      "questionId": "8b2e9c4d-71fa-4d8b-9a3e-5f6c8d9e2a1b",
      "selectedOptionId": "c9d3e4f5-82ab-4c9d-8e7f-6a5b4c3d2e1f",
      "valueText": null
    }
  ]
}
```

## 🔍 Campo `valueText`

### Propósito:
Permite enviar um valor textual adicional junto com a resposta selecionada.

### Casos de Uso:
1. **Questões com "Outro"**: Quando usuário seleciona "Outro" e precisa especificar
2. **Respostas abertas**: Complemento à resposta de múltipla escolha
3. **Observações**: Notas adicionais sobre a resposta

### Implementado como:
- **Opcional** (`String?`)
- **Valor padrão**: `null`
- Se não fornecido, será omitido ou enviado como `null` no JSON

### No nosso caso (IVCF-20):
- ❌ Não usamos (todas questões são múltipla escolha)
- ✅ Enviamos como `null` (sem problemas)
- ✅ API aceita conforme schema

## ✅ Validação Final

### Checklist da Implementação:

- [x] Todos os campos obrigatórios presentes
- [x] Tipos corretos (String, List)
- [x] @SerializedName configurado
- [x] Campo opcional `valueText` adicionado
- [x] Valores default apropriados
- [x] Estrutura corresponde ao schema da API

## 🎯 Compatibilidade

### ✅ Compatível com:
- Schema documentado na imagem
- Postman collection (linha 936)
- Backend API `/questionnaires/response`

### ✅ Validações:
- Tipos: OK
- Nomes: OK
- Estrutura: OK
- Campos opcionais: OK

## 📌 Observações

### `valueText` vs `note` local:
- **API**: usa `valueText` (opcional)
- **Local**: usamos `note` no modelo Answer
- **Mapeamento**: `valueText` sempre `null` no IVCF-20
  - Poderia ser mapeado de `answer.note` se necessário

### Possível Melhoria Futura:
Se o IVCF-20 adicionar questões que permitem observações:

```kotlin
// No ViewModel, ao criar AnswerRequest:
AnswerRequest(
    questionId = questionUuid,
    selectedOptionId = optionUuid,
    valueText = answer.note  // ✅ Usar note se disponível
)
```

## ✅ Conclusão

**Status:** ✅ **CORRETO E COMPLETO**

O payload implementado:
1. ✅ Contém todos os campos obrigatórios
2. ✅ Tem o campo opcional `valueText`
3. ✅ Corresponde ao schema da API
4. ✅ Está pronto para uso

---

**Data da verificação**: 2025-12-18  
**Endpoint**: POST `/questionnaires/response`  
**Status**: ✅ Verificado e Aprovado
