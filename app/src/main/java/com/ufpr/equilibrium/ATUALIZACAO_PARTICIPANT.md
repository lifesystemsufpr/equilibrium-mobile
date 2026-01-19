# 🔄 Atualização COMPLETA: Patient → Participant

## 🎯 Resumo da Atualização

Foi realizada a correção **completa** de TODOS os endpoints para usar `participant` e `participantId` ao invés de `patient` e `patientId`, conforme atualização da API backend.

## ✅ Alterações Realizadas

### 📄 1. PessoasAPI.kt

**Endpoints de Participant:**
```kotlin
// ANTES:
@GET("patient")
@POST("patient")

// DEPOIS:
@GET("participant")
@POST("participant")
```

**Query params de Evaluation:**
```kotlin
// ANTES:
@GET("evaluation")
fun getEvaluations(@Query("patientId") patientId: String)

// DEPOIS:
@GET("evaluation")
fun getEvaluations(@Query("participantId") participantId: String)
```

### 📄 2. Teste.kt

```kotlin
// ANTES:
data class Teste(
    val patientId: String,
    ...
)

// DEPOIS:
data class Teste(
    val participantId: String,
    ...
)
```

### 📄 3. EvaluationResponse.kt

```kotlin
// ANTES:
data class EvaluationResponse(
    val patientId: String?,
    val patient: PatientDto?
)

// DEPOIS:
data class EvaluationResponse(
    val participantId: String?,
    val participant: PatientDto?
)
```

### 📄 4. Timer.kt

```kotlin
// ANTES:
val teste = Teste(
    patientId = patientUuid.toString(),
    ...
)

// DEPOIS:
val teste = Teste(
    participantId = patientUuid.toString(),
    ...
)
```

## 📝 Endpoints Atualizados

### ✅ Endpoints de Participante
- ✅ `GET /participant` - Listar participantes
- ✅ `GET /participant/{id}` - Buscar participante por ID
- ✅ `POST /participant` - Criar participante
- ✅ `PATCH /participant/{id}` - Atualizar participante
- ✅ `DELETE /participant/{id}` - Deletar participante

### ✅ Endpoints de Avaliação - **AGORA USA participantId**
- ✅ `POST /evaluation` - Criar avaliação (usa `participantId` no body)
- ✅ `GET /evaluation?participantId={id}` - Buscar avaliações por participante

### ✅ Endpoints de Questionário
- ✅ `POST /questionnaires/response` - Enviar respostas (usa `participantId`)
- ✅ `GET /questionnaires/participant/{id}` - Histórico do participante

## 🔍 Arquivos Modificados

### ✅ Todos os arquivos foram corrigidos:
1. ✅ `PessoasAPI.kt` - Endpoints corrigidos
2. ✅ `Teste.kt` - Campo renomeado para `participantId`
3. ✅ `EvaluationResponse.kt` - Campo renomeado para `participantId` e `participant`
4. ✅ `Timer.kt` - Criação de Teste corrigida
5. ✅ Payloads de Questionário - Já estavam corretos

## 📊 Mapeamento Final de Campos

| Contexto | Endpoint | Campo no Body/Query |
|----------|----------|---------------------|
| Cadastro de Participante | `/participant` | N/A (objeto completo) |
| Questionário | `/questionnaires/response` | `participantId` ✅ |
| Questionário | `/questionnaires/participant/{id}` | path param ✅ |
| Avaliação 30sSTS | `/evaluation` | `participantId` ✅ |
| Consulta Avaliações | `/evaluation?participantId={id}` | `participantId` ✅ |

## 📋 Exemplo de Payload Correto (POST /evaluation)

```json
{
  "type": "TTSTS",
  "date": "2025-12-18T14:30:00.000Z",
  "time_init": "2025-12-18T14:30:10.000Z",
  "time_end": "2025-12-18T14:30:40.000Z",
  "participantId": "9526690b-e2e4-42bb-bf14-7c4c92dd70e3",
  "healthProfessionalId": "1633396f-e11f-4017-9caf-fef3538c15ac",
  "healthcareUnitId": "6cd3a9bf-17fa-4850-a326-8355872fd6c2",
  "sensorData": [...]
}
```

## ⚠️ Erro que Motivou a Correção

```json
{
  "message": [
    "participantId should not be empty",
    "participantId must be a string",
    "participantId must be a UUID"
  ],
  "error": "Bad Request",
  "statusCode": 400
}
```

Este erro indicou que a API backend foi atualizada para aceitar **apenas** `participantId` em todos os endpoints.

## ✨ Conclusão

- ✅ **TODOS** os endpoints agora usam `/participant` ao invés de `/patient`
- ✅ **TODOS** os campos agora usam `participantId` ao invés de `patientId`
- ✅ Application mobile totalmente sincronizada com a API backend
- ✅ Erro 400 resolvido

## 📌 Observação Importante

O nome da classe `PatientDto` foi mantido por compatibilidade com o código existente, mas o campo JSON serializado é `participant`. Isso não afeta o funcionamento da API.

---

**Data da atualização**: 2025-12-18  
**Motivo**: Atualização da API backend para nomenclatura consistente  
**Status**: ✅ Completo e Testado
