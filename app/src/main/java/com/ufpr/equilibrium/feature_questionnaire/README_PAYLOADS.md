# Payloads de Questionário - Documentação

## Visão Geral

Esta implementação fornece os payloads necessários para integração com a API de questionários do backend Tecnoaging, seguindo a estrutura definida na collection do Postman.

## Estrutura de Arquivos

```
feature_questionnaire/
├── payloads/
│   ├── QuestionnaireResponseRequest.kt      # Request para enviar respostas
│   ├── QuestionnaireStructureResponse.kt    # Response da estrutura do questionário
│   └── QuestionnaireResponsesResponse.kt    # Response de respostas do participante
├── api/
│   └── QuestionnaireAPI.kt                  # Interface Retrofit para endpoints
├── repository/
│   └── QuestionnaireRepository.kt           # Repositório para gerenciar API calls
└── mappers/
    └── QuestionnaireMapper.kt               # Conversores entre DTOs e modelos locais
```

## Endpoints Implementados

### 1. Submeter Respostas de Questionário
**POST** `/questionnaires/response`

**Request:**
```kotlin
QuestionnaireResponseRequest(
    participantId: String,
    healthProfessionalId: String,
    questionnaireId: String,
    answers: List<AnswerRequest>
)
```

**Response:**
```kotlin
QuestionnaireSubmitResponse(
    id: String,
    participantId: String,
    healthProfessionalId: String,
    questionnaireId: String,
    createdAt: String,
    totalScore: Int?,
    message: String?
)
```

### 2. Buscar Respostas por Participante
**GET** `/questionnaires/participant/{participantId}`

**Response:**
```kotlin
List<QuestionnaireResponsesResponse>
```

### 3. Buscar Estrutura do Questionário IVCF-20
**GET** `/questionnaires/ivcf-20`

**Response:**
```kotlin
QuestionnaireStructureResponse(
    id: String,
    name: String,
    description: String?,
    questions: List<QuestionDto>
)
```

### 4. Buscar Detalhes de uma Resposta
**GET** `/questionnaires/response/{responseId}`

**Response:**
```kotlin
QuestionnaireDetailResponse
```

## Uso do Repositório

### Exemplo: Submeter Respostas

```kotlin
val repository = QuestionnaireRepository()
val token = "seu_token_aqui"

// Criar mapa de respostas (questionId -> optionId)
val answersMap = mapOf(
    "512c1ba0-b3d3-434b-afe5-e3d9f8b344b8" to "09086cb8-0f47-4a15-9f2f-0f953dd6d1e2",
    "1a294ad8-0b70-4669-97e2-f8366a60341d" to "4fc15a8a-1c4c-45f9-9b9a-b360cd69d93c"
)

// Criar request
val request = repository.createSubmissionRequest(
    participantId = "0a775d40-65c3-4514-ad1e-d31f023a2191",
    healthProfessionalId = "18b0b378-1060-42d0-8d82-4a11ba7d2cee",
    questionnaireId = "9825800d-6ec8-4220-ad50-eeb10a84c337",
    answersMap = answersMap
)

// Enviar
repository.submitQuestionnaireResponse(
    request = request,
    token = token,
    onSuccess = { response ->
        println("Respostas enviadas com sucesso! ID: ${response.id}")
        println("Pontuação total: ${response.totalScore}")
    },
    onError = { error ->
        println("Erro: $error")
    }
)
```

### Exemplo: Buscar Estrutura do Questionário

```kotlin
repository.getIVCF20QuestionnaireStructure(
    token = token,
    onSuccess = { structure ->
        println("Questionário: ${structure.name}")
        println("Total de questões: ${structure.questions.size}")
        
        // Converter para modelos locais
        val localQuestions = QuestionnaireMapper.mapToLocalQuestions(structure)
        
        // Criar mapeamento de IDs
        val idMapping = QuestionnaireMapper.createIdMapping(structure)
    },
    onError = { error ->
        println("Erro: $error")
    }
)
```

### Exemplo: Buscar Respostas de um Participante

```kotlin
repository.getQuestionnaireResponsesByParticipant(
    participantId = "0a775d40-65c3-4514-ad1e-d31f023a2191",
    token = token,
    onSuccess = { responses ->
        println("Encontradas ${responses.size} respostas")
        responses.forEach { response ->
            println("ID: ${response.id}, Score: ${response.totalScore}")
        }
    },
    onError = { error ->
        println("Erro: $error")
    }
)
```

## Mappers

Os mappers facilitam a conversão entre os DTOs da API e os modelos locais:

```kotlin
// Converter estrutura da API para questões locais
val localQuestions = QuestionnaireMapper.mapToLocalQuestions(apiResponse)

// Criar mapeamento de IDs (local -> API)
val idMapping = QuestionnaireMapper.createIdMapping(apiResponse)

// Criar mapeamento reverso (API -> local)
val reverseMapping = QuestionnaireMapper.createReverseIdMapping(apiResponse)
```

## Configuração do Retrofit

A instância do `QuestionnaireAPI` foi adicionada ao `RetrofitClient`:

```kotlin
val questionnaireApi = RetrofitClient.instanceQuestionnaireAPI
```

## Autenticação

Todos os endpoints requerem autenticação via Bearer Token. O token é automaticamente adicionado ao header através do `AuthInterceptor` configurado no `RetrofitClient`.

## Notas Importantes

1. **UUIDs**: Todos os IDs (participantId, questionnaireId, etc.) devem ser UUIDs válidos
2. **Formato de Data**: As datas são retornadas em formato ISO 8601 (ex: "2025-12-18T14:30:00.000Z")
3. **Pontuação**: A pontuação total pode ser calculada no backend ou no cliente, dependendo das regras de negócio
4. **Grupos Especiais**: O grupo "avd_instrumental" tem regras especiais de pontuação (máximo de 4 pontos para 3 questões)

## Próximos Passos

Para integração completa:

1. Atualizar o `QuestionnaireViewModel` para usar o repositório
2. Implementar sincronização entre respostas locais e servidor
3. Adicionar tratamento de erros e retry logic
4. Implementar cache de estrutura do questionário
5. Adicionar testes unitários para mappers e repository

## Exemplo de Payload Completo (POST)

```json
{
    "participantId": "0a775d40-65c3-4514-ad1e-d31f023a2191",
    "healthProfessionalId": "18b0b378-1060-42d0-8d82-4a11ba7d2cee",
    "questionnaireId": "9825800d-6ec8-4220-ad50-eeb10a84c337",
    "answers": [
        {
            "questionId": "512c1ba0-b3d3-434b-afe5-e3d9f8b344b8",
            "selectedOptionId": "09086cb8-0f47-4a15-9f2f-0f953dd6d1e2"
        },
        {
            "questionId": "1a294ad8-0b70-4669-97e2-f8366a60341d",
            "selectedOptionId": "4fc15a8a-1c4c-45f9-9b9a-b360cd69d93c"
        }
    ]
}
```

## Referências

- Postman Collection: `Tecnoaging.postman_collection.json`
- Base URL: `https://tecnoaging.com.br/backend/`
- Documentação da API: Consulte a collection do Postman para detalhes completos
