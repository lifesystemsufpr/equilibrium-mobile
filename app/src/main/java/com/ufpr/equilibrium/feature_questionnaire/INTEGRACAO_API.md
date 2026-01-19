# 📋 Integração do Envio de Questionário IVCF-20

## ✅ Resumo da Implementação

Foi implementada a funcionalidade completa de envio das respostas do questionário IVCF-20 para o backend através da API `/questionnaires/response`.

## 🔧 Arquivos Modificados

### 1. **QuestionnaireViewModel.kt**

#### ✅ Adições:
- **Repository Injection**: Injetado `QuestionnaireRepository` para comunicação com a API
- **ID Mappings**: Criados mapeamentos entre IDs locais e IDs da API
  - `apiQuestionnaireId`: ID do questionário no backend
  - `questionIdMapping`: Map de índices locais → UUIDs da API
  - `optionIdMapping`: Map de índices de opções → UUIDs da API

#### ✅ Novo Método: `submitAnswers()`
```kotlin
fun submitAnswers(
    participantId: String,
    healthProfessionalId: String,
    token: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
)
```

**Funcionamento:**
1. **Carrega estrutura da API** (se necessário)
   - Faz GET `/questionnaires/ivcf-20`
   - Cria mapeamentos de IDs
2. **Converte respostas locais para formato da API**
   - Transforma índices em UUIDs
3. **Envia para o backend**
   - POST `/questionnaires/response`
   - Payload com `participantId`, `healthProfessionalId`, `questionnaireId`, e `answers`

### 2. **QuestionnaireActivity.kt**

#### ✅ Modificações no botão "Finalizar":
- **Antes**: Mostrava apenas o resultado e fechava
- **Agora**: 
  1. Calcula e mostra o score
  2. Oferece opção "Enviar" ou "Cancelar"
  3. Ao clicar em "Enviar", chama `submitQuestionnaire()`

#### ✅ Novo Método: `submitQuestionnaire()`
```kotlin
private fun submitQuestionnaire()
```

**Funcionamento:**
1. Valida IDs do participante e profissional
2. Mostra feedback de loading (Toast)
3. Chama `viewModel.submitAnswers()`
4. Trata sucesso: Mostra dialog e fecha activity
5. Trata erro: Permite retry ou cancelamento

## 📊 Fluxo Completo

```
Usuário responde questionário
         ↓
Clica em "Finalizar"
         ↓
Valida se todas questões foram respondidas
         ↓
Calcula score e interpretação
         ↓
Mostra resultado com opção "Enviar" ou "Cancelar"
         ↓
[Usuário clica em "Enviar"]
         ↓
Valida participantId e professionalId
         ↓
ViewModel carrega estrutura da API (1ª vez)
         ↓
Cria mapeamentos IDs locais ↔ UUIDs API
         ↓
Converte respostas para formato API
         ↓
POST /questionnaires/response
         ↓
[SUCESSO] → Mostra mensagem e fecha
[ERRO] → Permite retry ou cancelar
```

## 📝 Exemplo de Payload Enviado

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
    // ... mais 18 respostas (total de 20 questões IVCF-20)
  ]
}
```

## 🔐 Segurança e Validações

### ✅ Validações Implementadas:

1. **Participante identificado**
   - Verifica `PacienteManager.uuid`
   - Erro se null: "Participante não identificado"

2. **Usuário autenticado**
   - Verifica `SessionManager.user?.id` e `SessionManager.token`
   - Erro se null: "Usuário não autenticado"

3. **Todas questões respondidas**
   - Validado antes de permitir finalizar
   - Usa `ValidateCompletionUseCase`

4. **Token Bearer**
   - Enviado automaticamente no header via `AuthInterceptor`

## ⚙️ Configuração do Repositório

O `QuestionnaireRepository` é injetado via Hilt/Dagger:

```kotlin
@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    ...,
    private val questionnaireRepository: QuestionnaireRepository
)
```

**Obs**: Certifique-se de que `QuestionnaireRepository` está configurado no módulo Hilt.

## 📱 UX/UI

### Estados de Loading:
- **Enviando**: Toast "Enviando questionário..."
- **Botão desabilitado**: `fabFinish.isEnabled = false`

### Dialogs:
1. **Resultado**: Score + Interpretação + Botões "Enviar"/"Cancelar"
2. **Sucesso**: "Questionário enviado com sucesso! ID: {responseId}"
3. **Erro**: Mensagem de erro + Opções "Tentar Novamente"/"Cancelar"

## 🐛 Tratamento de Erros

### Possíveis Erros:

1. **400 Bad Request**
   - IDs inválidos ou campos faltando
   - Mensagem: Detalhes do erro da API

2. **401 Unauthorized**
   - Token inválido ou expirado
   - Mensagem: "Usuário não autenticado"

3. **404 Not Found**
   - Questionário ou participante não encontrado
   - Mensagem: Erro da API

4. **500 Server Error**
   - Erro no servidor
   - Mensagem: Detalhes do erro

5. **Network Error**
   - Sem conexão
   - Mensagem: "Falha na conexão"

### Retry:
- Usuário pode tentar reenviar em caso de erro
- Mantém as respostas em memória

## ✨ Melhorias Futuras Sugeridas

1. **Persistência Offline**
   - Salvar respostas localmente se não houver conexão
   - Sincronizar quando reconectar

2. **Progress Dialog**
   - Adicionar ProgressBar no layout
   - Mostrar loading visual melhor

3. **Cache da Estrutura**
   - Persistir mapeamentos de IDs
   - Re-carregar apenas quando necessário

4. **Validação de Dados**
   - Verificar se todos os campos obrigatórios estão presentes
   - Validar formato de UUIDs

5. **Logs e Analytics**
   - Log de envios bem-sucedidos
   - Tracking de erros

## 📋 Checklist de Testes

- [ ] Responder todas as 20 questões do IVCF-20
- [ ] Verificar cálculo correto do score
- [ ] Testar envio com conexão ativa
- [ ] Testar comportamento sem conexão
- [ ] Verificar se IDs corretos são enviados
- [ ] Testar retry em caso de erro
- [ ] Verificar que participante está selecionado
- [ ] Verificar que usuário está autenticado
- [ ] Testar cancelar envio
- [ ] Verificar mensagens de sucesso/erro

---

**Data da implementação**: 2025-12-18  
**Endpoint utilizado**: POST `/questionnaires/response`  
**Status**: ✅ Implementado e Pronto para Testes
