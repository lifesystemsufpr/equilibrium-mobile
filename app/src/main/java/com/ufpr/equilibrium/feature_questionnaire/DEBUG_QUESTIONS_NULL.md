# 🔍 Debug: Questões Null com Response 200

## 🐛 Problema Reportado

**Sintoma:** API retorna HTTP 200 (sucesso), mas o app mostra erro "Estrutura do questionário não contém questões"

**Causa Provável:** O campo `questions` está vindo `null` ou vazio no JSON, apesar do status 200

## 📊 Logs Adicionados

### Logs que aparecerão no Logcat:

```
QuestionnaireRepo: Response code: 200
QuestionnaireRepo: Response successful: true
QuestionnaireRepo: Response body null: false
QuestionnaireRepo: Structure ID: <id-do-questionario>
QuestionnaireRepo: Structure name: <nome-do-questionario>
QuestionnaireRepo: Questions null: true/false
QuestionnaireRepo: Questions size: 0 ou <numero>
QuestionnaireRepo: Raw JSON response: {...}
```

## 🔎 Diagnóstico

### Cenário 1: Questions null
```
Questions null: true
Questions size: 0
```
**Causa:** Campo `questions` não existe no JSON ou vem como `null`

**JSON Recebido:**
```json
{
  "id": "uuid",
  "name": "IVCF-20",
  "description": "...",
  "questions": null
}
```

### Cenário 2: Questions vazio
```
Questions null: false
Questions size: 0
```
**Causa:** Campo `questions` existe mas é um array vazio

**JSON Recebido:**
```json
{
  "id": "uuid",
  "name": "IVCF-20",
  "description": "...",
  "questions": []
}
```

### Cenário 3: Nome do campo diferente
```
Questions null: true
```
**Causa:** O backend usa nome diferente (ex: `question`, `items`, `questionList`)

**JSON Recebido:**
```json
{
  "id": "uuid",
  "name": "IVCF-20",
  "questionList": [...]  // ❌ não é "questions"
}
```

## 🔧 Soluções Possíveis

### Solução 1: Questionário não cadastrado no backend
Se o questionário IVCF-20 não está cadastrado no banco de dados:

**Ação:**
1. Acessar painel admin do backend
2. Cadastrar questionário IVCF-20 com todas as 20 questões
3. Verificar que cada questão tem suas opções

### Solução 2: Nome do campo diferente
Se o campo no JSON tem nome diferente:

**Verificar no Raw JSON log** qual é o nome correto do campo

**Ajustar QuestionnaireStructureResponse.kt:**
```kotlin
data class QuestionnaireStructureResponse(
    @SerializedName("questions")  // ✅ Nome atual
    // OU
    @SerializedName("questionList")  // Se for esse o nome real
    // OU
    @SerializedName("items")  // Se for esse o nome real
    val questions: List<QuestionDto>?
)
```

### Solução 3: Estrutura aninhada
Se as questões estão dentro de outro objeto:

**JSON Recebido:**
```json
{
  "id": "uuid",
  "name": "IVCF-20",
  "data": {
    "questions": [...]
  }
}
```

**Ajustar estrutura:**
```kotlin
data class QuestionnaireStructureResponse(
    val id: String,
    val name: String,
    val data: QuestionnaireData?
)

data class QuestionnaireData(
    val questions: List<QuestionDto>?
)
```

### Solução 4: Endpoint incorreto
Verificar se o endpoint está correto:

**Atual:** `GET /questionnaires/ivcf-20`

**Verificar se não deveria ser:**
- `GET /questionnaire/ivcf-20`
- `GET /questionnaires/structure/ivcf-20`
- `GET /questionnaires` (buscar todos e filtrar)

## 📋 Checklist de Verificação

Execute os testes e verifique os logs:

### 1. Verificar Response Code
```
✅ Response code: 200
✅ Response successful: true
```

### 2. Verificar Body
```
✅ Response body null: false
✅ Structure ID: não vazio
✅ Structure name: não vazio
```

### 3. Verificar Questions
```
❌ Questions null: true  → PROBLEMA AQUI
ou
❌ Questions size: 0     → PROBLEMA AQUI
```

### 4. Analisar Raw JSON
```
QuestionnaireRepo: Raw JSON response: {...}
```
**Copie este JSON e analise:**
- O campo `questions` existe?
- Qual o nome exato do campo?
- É um array?
- Tem elementos?

## 🛠️ Como Proceder

### Passo 1: Execute o app e tente enviar questionário
Isso gerará os logs

### Passo 2: Filtre os logs por "QuestionnaireRepo"
```bash
adb logcat -s QuestionnaireRepo
```

### Passo 3: Copie o "Raw JSON response"
Analise a estrutura real do JSON

### Passo 4: Compare com o esperado
**Esperado:**
```json
{
  "id": "9825800d-6ec8-4220-ad50-eeb10a84c337",
  "name": "IVCF-20",
  "description": "Índice de Vulnerabilidade Clínico Funcional",
  "questions": [
    {
      "id": "512c1ba0-b3d3-434b-afe5-e3d9f8b344b8",
      "text": "Qual a sua idade?",
      "order": 1,
      "groupId": "idade",
      "groupName": "Idade",
      "options": [
        {
          "id": "09086cb8-0f47-4a15-9f2f-0f953dd6d1e2",
          "text": "60 a 74 anos",
          "score": 0
        },
        {
          "id": "4fc15a8a-1c4c-45f9-9b9a-b360cd69d93c",
          "text": "75 a 84 anos",
          "score": 1
        },
        {
          "id": "af38bb52-d80c-4ece-aa20-e451236cd5cb",
          "text": "85 anos ou mais",
          "score": 3
        }
      ]
    },
    // ... mais 19 questões
  ]
}
```

### Passo 5: Ajuste o código conforme necessário
Baseado no JSON real recebido, ajuste:
1. Nome dos campos (@SerializedName)
2. Estrutura das classes (se aninhado)
3. Endpoint (se estiver errado)

## 💡 Dicas Adicionais

### Testar endpoint manualmente
Use o Postman ou curl para testar:

```bash
curl -H "Authorization: Bearer SEU_TOKEN" \
     https://tecnoaging.com.br/backend/questionnaires/ivcf-20
```

Compare o resultado com o que o app está recebendo.

### Verificar no Postman Collection
O arquivo `Tecnoaging.postman_collection.json` tem o exemplo de resposta esperada na linha 1009.

### Backend em desenvolvimento
Se o backend ainda não tem o questionário cadastrado:
1. Criar no banco de dados
2. Popular com as 20 questões do IVCF-20
3. Cada questão com suas opções e scores

## 📝 Próximos Passos

1. [ ] Executar app e gerar logs
2. [ ] Copiar "Raw JSON response" do Logcat
3. [ ] Analisar estrutura recebida
4. [ ] Comparar com estrutura esperada
5. [ ] Identificar diferença
6. [ ] Aplicar correção apropriada
7. [ ] Testar novamente

---

**Status:** 🔍 Em Investigação  
**Prioridade:** Alta  
**Próxima ação:** Analisar logs do Logcat
