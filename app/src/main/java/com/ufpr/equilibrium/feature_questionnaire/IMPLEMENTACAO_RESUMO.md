# 📋 Implementação de Payloads de Questionário - Resumo

## ✅ Arquivos Criados

### 📁 payloads/ (3 arquivos)
1. **QuestionnaireResponseRequest.kt**
   - Request para enviar respostas do questionário
   - Contém: `QuestionnaireResponseRequest` e `AnswerRequest`

2. **QuestionnaireStructureResponse.kt**
   - Response da estrutura do questionário IVCF-20
   - Contém: `QuestionnaireStructureResponse`, `QuestionDto`, `OptionDto`

3. **QuestionnaireResponsesResponse.kt**
   - Responses para consultas de respostas
   - Contém: 
     - `QuestionnaireResponsesResponse`
     - `QuestionnaireSubmitResponse`
     - `QuestionnaireDetailResponse`
     - `AnswerResponse`
     - DTOs auxiliares (ParticipantDto, HealthProfessionalDto, QuestionnaireDto)

### 📁 api/ (1 arquivo)
4. **QuestionnaireAPI.kt**
   - Interface Retrofit com todos os endpoints
   - Métodos:
     - `submitQuestionnaireResponse()` - POST /questionnaires/response
     - `getQuestionnaireResponsesByParticipant()` - GET /questionnaires/participant/{id}
     - `getIVCF20QuestionnaireStructure()` - GET /questionnaires/ivcf-20
     - `getQuestionnaireResponseDetails()` - GET /questionnaires/response/{id}

### 📁 repository/ (1 arquivo)
5. **QuestionnaireRepository.kt**
   - Repositório para gerenciar chamadas de API
   - Implementa callbacks com onSuccess/onError
   - Inclui função auxiliar `createSubmissionRequest()`

### 📁 mappers/ (1 arquivo)
6. **QuestionnaireMapper.kt**
   - Conversores entre DTOs da API e modelos locais
   - Funções principais:
     - `mapToLocalQuestions()` - Converte estrutura da API
     - `createIdMapping()` - Mapeia IDs locais -> API
     - `createReverseIdMapping()` - Mapeia IDs API -> locais

### 📁 examples/ (1 arquivo)
7. **QuestionnairePayloadExamples.kt**
   - Exemplos práticos de uso
   - 6 exemplos completos documentados

### 📄 Documentação (1 arquivo)
8. **README_PAYLOADS.md**
   - Documentação completa
   - Guias de uso
   - Exemplos de código

### 🔧 Modificações em Arquivos Existentes
9. **RetrofitClient.kt**
   - Adicionada instância `instanceQuestionnaireAPI`

## 🎯 Funcionalidades Implementadas

### ✅ Endpoints da API
- [x] POST /questionnaires/response - Enviar respostas
- [x] GET /questionnaires/participant/{id} - Buscar histórico
- [x] GET /questionnaires/ivcf-20 - Obter estrutura
- [x] GET /questionnaires/response/{id} - Detalhes de resposta

### ✅ Recursos
- [x] Modelos de dados (DTOs) compatíveis com a API
- [x] Interface Retrofit configurada
- [x] Repositório com tratamento de erros
- [x] Mappers para conversão de dados
- [x] Exemplos de uso
- [x] Documentação completa

## 📊 Estrutura de Diretórios Completa

```
feature_questionnaire/
├── payloads/
│   ├── QuestionnaireResponseRequest.kt       ✅ Novo
│   ├── QuestionnaireStructureResponse.kt     ✅ Novo
│   └── QuestionnaireResponsesResponse.kt     ✅ Novo
├── api/
│   └── QuestionnaireAPI.kt                   ✅ Novo
├── repository/
│   └── QuestionnaireRepository.kt            ✅ Novo
├── mappers/
│   └── QuestionnaireMapper.kt                ✅ Novo
├── examples/
│   └── QuestionnairePayloadExamples.kt       ✅ Novo
├── README_PAYLOADS.md                        ✅ Novo
├── AnswerDAO.kt                              (existente)
├── AnswerEntity.kt                           (existente)
├── IVCF20Questions.kt                        (existente)
├── Option.kt                                 (existente)
├── Question.kt                               (existente)
├── QuestionGroup.kt                          (existente)
├── QuestionnaireActivity.kt                  (existente)
├── QuestionnaireAdapter.kt                   (existente)
├── QuestionnaireDatabase.kt                  (existente)
└── QuestionnaireViewModel.kt                 (existente)
```

## 🚀 Como Usar

### 1. Inicializar o Repositório
```kotlin
val repository = QuestionnaireRepository()
val token = "Bearer your_token_here"
```

### 2. Carregar Estrutura do Questionário
```kotlin
repository.getIVCF20QuestionnaireStructure(
    token = token,
    onSuccess = { structure ->
        val questions = QuestionnaireMapper.mapToLocalQuestions(structure)
        // Use as questões na UI
    },
    onError = { error -> /* Tratar erro */ }
)
```

### 3. Enviar Respostas
```kotlin
val request = repository.createSubmissionRequest(
    participantId = "uuid-do-participante",
    healthProfessionalId = "uuid-do-profissional",
    questionnaireId = "uuid-do-questionario",
    answersMap = mapOf(
        "question-uuid-1" to "option-uuid-1",
        "question-uuid-2" to "option-uuid-2"
    )
)

repository.submitQuestionnaireResponse(
    request = request,
    token = token,
    onSuccess = { response ->
        println("Score: ${response.totalScore}")
    },
    onError = { error -> /* Tratar erro */ }
)
```

## 📝 Próximos Passos Sugeridos

### 1. Integração com ViewModel
- [ ] Atualizar `QuestionnaireViewModel` para usar o repositório
- [ ] Implementar sincronização entre dados locais e servidor
- [ ] Adicionar estados de loading/error no UI

### 2. Persistência
- [ ] Salvar estrutura do questionário localmente (cache)
- [ ] Sincronizar respostas offline
- [ ] Implementar queue de envio

### 3. Testes
- [ ] Testes unitários para mappers
- [ ] Testes de integração para repository
- [ ] Mocks da API para testes

### 4. UI/UX
- [ ] Indicadores de progresso ao enviar
- [ ] Mensagens de erro amigáveis
- [ ] Confirmação de envio bem-sucedido

## 🔑 Pontos Importantes

1. **Autenticação**: Todos os endpoints requerem Bearer token
2. **UUIDs**: Todos os IDs são UUIDs no formato string
3. **Formato de Data**: ISO 8601 (ex: "2025-12-18T14:30:00.000Z")
4. **Pontuação**: A API pode retornar totalScore calculado
5. **Grupos Especiais**: AVD Instrumental tem regras especiais de pontuação

## 📚 Referências

- **Postman Collection**: `Tecnoaging.postman_collection.json`
- **Base URL**: `https://tecnoaging.com.br/backend/`
- **Documentação Detalhada**: `README_PAYLOADS.md`
- **Exemplos de Código**: `examples/QuestionnairePayloadExamples.kt`

## ✨ Características da Implementação

- ✅ **Type-Safe**: Todos os modelos são fortemente tipados
- ✅ **Null-Safe**: Uso adequado de tipos nullable
- ✅ **Documentado**: Comentários KDoc em todas as classes
- ✅ **Modular**: Separação clara de responsabilidades
- ✅ **Testável**: Estrutura facilita criação de testes
- ✅ **Extensível**: Fácil adicionar novos endpoints

## 👥 Contribuição

Para adicionar novos endpoints de questionário:

1. Adicione o DTO em `payloads/`
2. Adicione o método na interface `QuestionnaireAPI`
3. Implemente a função no `QuestionnaireRepository`
4. Crie mapper se necessário em `QuestionnaireMapper`
5. Adicione exemplo em `QuestionnairePayloadExamples`
6. Atualize a documentação

---

**Status**: ✅ Implementação Completa  
**Versão**: 1.0  
**Data**: 2025-12-18  
**Compatível com**: API Tecnoaging v1
