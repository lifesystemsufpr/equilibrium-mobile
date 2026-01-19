# Refatoração de API: Patient/Paciente → Participant

## ✅ Concluído

Refatoração **focada apenas em métodos de requisição da API**, mantendo classes de modelo inalteradas para compatibilidade.

---

## Arquivos Modificados

### 1. App Module - PessoasAPI.kt
**Arquivo**: `app/src/main/java/com/ufpr/equilibrium/network/PessoasAPI.kt`

**Mudanças**:
- ✅ `getPacientes()` → `getParticipants()`
- ✅ `postPatient()` → `postParticipant()`
- ✅ Comentários atualizados para inglês
- ✅ Adicionado comentário sobre compatibilidade de models

```diff
- fun getPacientes(...): Call<PacientesEnvelope>
+ fun getParticipants(...): Call<PacientesEnvelope>

- fun postPatient(...): Call<PacienteModel>
+ fun postParticipant(...): Call<PacienteModel>
```

### 2. Data Module - PessoasService.kt
**Arquivo**: `data/src/main/java/com/ufpr/equilibrium/data/remote/PessoasService.kt`

**Mudanças**:
- ✅ `getPatients()` → `getParticipants()`  
- ✅ `postPatient()` → `postParticipant()`
- ✅ Comentários atualizados (Patient → Participant)
- ✅ Envelope comment atualizado

```diff
- suspend fun getPatients(...): PatientsEnvelope
+ suspend fun getParticipants(...): PatientsEnvelope

- suspend fun postPatient(...): PatientRegistrationDto
+ suspend fun postParticipant(...): PatientRegistrationDto
```

### 3. App - ListagemPacientes.kt
**Arquivo**: `app/src/main/java/com/ufpr/equilibrium/feature_professional/ListagemPacientes.kt`

**Mudanças**: Atualização de chamadas de API

```diff
- pessoasAPI.getPacientes(cpf = cpf, ...)
+ pessoasAPI.getParticipants(cpf = cpf, ...)

- pessoasAPI.getPacientes(page = page, ...)
+ pessoasAPI.getParticipants(page = page, ...)
```

### 4. Data - PatientRepositoryImpl.kt
**Arquivo**: `data/src/main/java/com/ufpr/equilibrium/data/repository/PatientRepositoryImpl.kt`

**Mudanças**: Atualização de chamadas service

```diff
- service.getPatients(page, pageSize)
+ service.getParticipants(page, pageSize)

- service.getPatients(cpf = cpf)
+ service.getParticipants(cpf = cpf)

- service.postPatient(dto)
+ service.postParticipant(dto)
```

---

## O Que NÃO Foi Alterado

Para manter compatibilidade e evitar breaking changes:

### Classes de Modelo (Mantidas)
- ✅ `PacienteModel` - **NÃO mudado**
- ✅ `PacientesEnvelope` - **NÃO mudado**
- ✅ `PatientDto` - **NÃO mudado**
- ✅ `PatientRegistrationDto` - **NÃO mudado**
- ✅ `Patient` (domain) - **NÃO mudado**

### Outros
- ✅ Nomes de arquivos - **NÃO mudados**
- ✅ Pacotes - **NÃO mudados**
- ✅ Layouts XML - **NÃO mudados**
- ✅ Strings resources - **NÃO mudadas**

---

## Endpoints da API

Os endpoints da API **permanecem os mesmos**:

```kotlin
@GET("participant")      // Backend usa "participant"
@POST("participant")     // Backend usa "participant"
@GET("patient")          // Backend usa "patient" (data module)
```

---

## Verificação de Build

### ✅ Checklist Pré-Build
- [x] Métodos renomeados em interfaces Retrofit
- [x] Chamadas atualizadas em todas as classes
- [x] Models **não alterados** (compatibilidade API)
- [x] Nenhuma quebra de imports

### Próximo Passo
Execute o build para validar:
```bash
./gradlew clean build
```

---

## Resumo

| Item | Antes | Depois |
|------|-------|--------|
| **Método GET** | `getPacientes()` ou `getPatients()` | `getParticipants()` |
| **Método POST** | `postPatient()` | `postParticipant()` |
| **Classes/Models** | `PacienteModel`, `PatientDto`, etc. | **SEM MUDANÇA** ✅ |
| **Endpoints API** | `/participant`, `/patient` | **SEM MUDANÇA** ✅ |

---

## Compatibilidade

✅ **API**: Endpoints não mudaram  
✅ **JSON**: Serialização/Deserialização mantida via `@SerializedName`  
✅ **Models**: Classes de dados intactas  
✅ **Build**: Deve compilar sem erros

---

## Benefícios

1. **Consistência**: Nomes de métodos agora refletem a terminologia correta ("participant")
2. **Clareza**: Código mais fácil de entender  
3. **Segurança**: Mudanças mínimas reduzem risco de quebra
4. **Compatibilidade**: API backend continua funcionando normalmente

---

## Status Final

✅ **Refatoração completa** - apenas métodos de API  
✅ **Nenhuma quebra de compatibilidade** com API  
✅ **Pronto para build e testes**
