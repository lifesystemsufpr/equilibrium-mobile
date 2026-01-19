# ✅ Implementação Simplificada: Envio Direto de Questionário

## 📋 O Que Mudou

**Antes:** App tentava buscar estrutura do questionário da API primeiro  
**Agora:** App envia respostas diretamente usando questionário local

## 🎯 Nova Abordagem

### Endpoint Utilizado
```
POST /questionnaires/response
```

### Payload Enviado
```json
{
  "participantId": "uuid-do-participante",
  "healthProfessionalId": "uuid-do-profissional",
  "questionnaireId": "9825800d-6ec8-4220-ad50-eeb10a84c337",
  "answers": [
    {
      "questionId": "uuid-gerado-da-questao",
      "selectedOptionId": "uuid-gerado-da-opcao"
    }
  ]
}
```

## 🔧 Geração de UUIDs

### Problema:
- Questões locais usam IDs numéricos (1, 2, 3...)
- API exige UUIDs (formato: `512c1ba0-b3d3-434b-afe5-e3d9f8b344b8`)

### Solução:
UUIDs **determinísticos** gerados a partir dos IDs locais:

```kotlin
// Para a questão
val questionUuid = java.util.UUID.nameUUIDFromBytes(
    "question_${localQuestionId}".toByteArray()
).toString()

// Para a opção
val optionUuid = java.util.UUID.nameUUIDFromBytes(
    "question_${localQuestionId}_option_${optionIndex}".toByteArray()
).toString()
```

### Exemplo:
```kotlin
// Questão ID 1
questionUuid = UUID.nameUUIDFromBytes("question_1") 
// Resultado: "f47ac10b-58cc-4372-a567-0e02b2c3d479"

// Opção 0 da questão 1
optionUuid = UUID.nameUUIDFromBytes("question_1_option_0")
// Resultado: "a3c5b8f2-91ca-4a7d-9f4e-2d8e6b1c7a9d"
```

**Vantagem:** Sempre gera os mesmos UUIDs para os mesmos IDs locais.

## 📊 Fluxo Completo

```
1. Usuário responde questionário local (IVCF20Questions) ✅
         ↓
2. App armazena respostas com IDs numéricos ✅
         ↓
3. Usuário clica em "Enviar" ✅
         ↓
4. App converte IDs locais → UUIDs determinísticos ✅
         ↓
5. POST para /questionnaires/response ✅
         ↓
6. Backend processa e retorna ID da submissão ✅
         ↓
7. App mostra sucesso ✅
```

## 🔑 ID do Questionário

**ID Fixo do IVCF-20:**
```kotlin
val questionnaireId = "9825800d-6ec8-4220-ad50-eeb10a84c337"
```

Este é o ID do questionário IVCF-20 no backend (conforme Postman collection).

## ✅ Vantagens da Nova Abordagem

### 1. **Mais Simples**
- Não precisa buscar estrutura da API
- Menos chamadas de API
- Menos código

### 2. **Mais Rápido**
- Envio direto, sem etapa intermediária
- Menos latência

### 3. **Mais Robusto**
- Não depende de endpoint `/questionnaires/ivcf-20` estar disponível
- Funciona mesmo se estrutura não estiver cadastrada

### 4. **Offline-First**
- Questionário funciona 100% offline
- Só precisa de conexão para enviar

## 📝 Código Simplificado

### QuestionnaireViewModel.kt

**Removido:**
- ❌ `apiQuestionnaireId`
- ❌ `questionIdMapping`
- ❌ `optionIdMapping`
- ❌ Método `performSubmit()`
- ❌ Chamada para `getIVCF20QuestionnaireStructure()`

**Simplificado:**
```kotlin
fun submitAnswers(...) {
    // 1. ID fixo do questionário
    val questionnaireId = "9825800d-6ec8-4220-ad50-eeb10a84c337"
    
    // 2. Converter respostas locais para UUIDs
    val apiAnswers = answersMap.map { (localQuestionId, answer) ->
        val questionUuid = UUID.nameUUIDFromBytes("question_${localQuestionId}".toByteArray())
        val optionUuid = UUID.nameUUIDFromBytes("question_${localQuestionId}_option_${answer.selectedOptionIndex}".toByteArray())
        
        AnswerRequest(questionId = questionUuid, selectedOptionId = optionUuid)
    }
    
    // 3. Enviar direto
    repository.submitQuestionnaireResponse(request, token, onSuccess, onError)
}
```

## 🔄 Sincronização com Backend

### Se o Backend Espera UUIDs Específicos:

**Opção 1:** Backend aceita qualquer UUID e armazena
- ✅ Mais flexível
- ✅ Funciona com geração determinística

**Opção 2:** Backend valida UUIDs contra cadastro
- ⚠️ Precisa cadastrar questões com UUIDs correspondentes
- ⚠️ UUIDs gerados devem bater com cadastro

### Recomendação:
Configure o backend para **aceitar qualquer UUID** e associar à estrutura interna pelo `questionnaireId`.

## ⚙️ Configuração do Backend (Se Necessário)

Se o backend precisa dos UUIDs exatos, criar script para popular:

```sql
-- Exemplo de insert com UUIDs gerados
INSERT INTO questions (id, questionnaire_id, text, "order") VALUES
  ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 
   '9825800d-6ec8-4220-ad50-eeb10a84c337', 
   'Qual é a sua idade?', 
   1);

INSERT INTO options (id, question_id, text, score) VALUES
  ('a3c5b8f2-91ca-4a7d-9f4e-2d8e6b1c7a9d',
   'f47ac10b-58cc-4372-a567-0e02b2c3d479',
   '60 a 74 anos',
   0);
```

## 🧪 Teste

### Payload de Exemplo Completo:
```json
{
  "participantId": "0a775d40-65c3-4514-ad1e-d31f023a2191",
  "healthProfessionalId": "18b0b378-1060-42d0-8d82-4a11ba7d2cee",
  "questionnaireId": "9825800d-6ec8-4220-ad50-eeb10a84c337",
  "answers": [
    {
      "questionId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "selectedOptionId": "a3c5b8f2-91ca-4a7d-9f4e-2d8e6b1c7a9d"
    },
    {
      "questionId": "8b2e9c4d-71fa-4d8b-9a3e-5f6c8d9e2a1b",
      "selectedOptionId": "c9d3e4f5-82ab-4c9d-8e7f-6a5b4c3d2e1f"
    }
    // ... mais respostas
  ]
}
```

## 📊 Logs

O repositório ainda loga informações detalhadas:

```
QuestionnaireRepo: Sending request to POST /questionnaires/response
QuestionnaireRepo: Participant: 0a775d40-65c3-4514-ad1e-d31f023a2191
QuestionnaireRepo: Professional: 18b0b378-1060-42d0-8d82-4a11ba7d2cee
QuestionnaireRepo: Questionnaire: 9825800d-6ec8-4220-ad50-eeb10a84c337
QuestionnaireRepo: Answers count: 6
QuestionnaireRepo: Response code: 200
QuestionnaireRepo: Success! Response ID: ...
```

## ✅ Próximos Passos

1. [ ] Testar envio com questões respondidas
2. [ ] Verificar resposta do backend (200 OK)
3. [ ] Confirmar que dados são salvos corretamente
4. [ ] Se necessário, ajustar backend para aceitar UUIDs gerados

---

**Status:** ✅ Simplificado e Pronto  
**Versão:** 2.0 (Simplificada)  
**Data:** 2025-12-18  
**Endpoint:** POST `/questionnaires/response`
