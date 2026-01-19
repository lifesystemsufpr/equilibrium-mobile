# Checklist de Validação Pré-AAB (App Bundle)

## ⚠️ IMPORTANTE
Este checklist deve ser executado **ANTES** de fazer upload de um novo AAB para a Google Play Store.

---

## 1. Verificações de Build

### 1.1. Build Release Compila
- [ ] Executar `./gradlew assembleRelease` sem erros
- [ ] Verificar que não há warnings críticos de ProGuard/R8
- [ ] Confirmar que APK foi gerado em `app/build/outputs/apk/release/`

### 1.2. ProGuard Mapping
- [ ] Verificar que arquivo `app/build/outputs/mapping/release/mapping.txt` foi gerado
- [ ] Confirmar que DTOs **NÃO** foram ofuscados (buscar por `SensorDataPoint`, `Teste`, `Usuario`, etc.)
- [ ] Salvar `mapping.txt` para futura deobfuscação de stack traces

### 1.3. APK Size
- [ ] Verificar que tamanho do APK é razoável (< 50MB recomendado)
- [ ] Comparar com versão anterior para detectar aumentos inesperados

---

## 2. Validações de Código

### 2.1. Tipos Genéricos Seguros
- [ ] **Nenhum uso de `Call<Any>`** em interfaces Retrofit
- [ ] **Nenhum uso de `Map<String, Any>`** em models de API
- [ ] **Nenhum uso de `List<Any>`** sem tipo específico
- [ ] Todos os DTOs usam tipos primitivos ou classes concretas

### 2.2. Anotações Gson
- [ ] Todos os DTOs têm `@SerializedName` em **todos os campos**
- [ ] Verificar:
  - `network/Teste.kt` ✓
  - `network/Usuario.kt` ✓
  - `network/SensorDataPoint.kt` ✓
  - `feature_professional/User.kt` ✓
  - `feature_professional/PacienteModel.kt` ✓
  - `feature_professional/ProfessionalModel.kt` ✓
  - `feature_healthUnit/HealthUnit.kt` ✓

### 2.3. ProGuard Rules
- [ ] `proguard-rules.pro` contém regras para:
  - `-keepattributes Signature,InnerClasses,EnclosingMethod`
  - `-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations`
  - `-keep class com.ufpr.equilibrium.network.** { *; }`
  - `-keep class com.ufpr.equilibrium.data.remote.dto.** { *; }`
  - `-keepclassmembers,allowobfuscation class * { @com.google.gson.annotations.SerializedName <fields>; }`

---

## 3. Testes de Integração (Build Release)

### 3.1. Instalação Manual
- [ ] Instalar APK release em dispositivo físico
- [ ] Confirmar que app abre sem crashes
- [ ] Verificar que todas as telas carregam corretamente

### 3.2. Testes de API (Todas as Requisições)

#### Autenticação
- [ ] ✅ Login de profissional funciona
- [ ] ✅ Login de participante funciona
- [ ] ✅ Token JWT é salvo corretamente
- [ ] ✅ Refresh token funciona (se aplicável)

#### Gestão de Pacientes
- [ ] ✅ Cadastro de novo paciente
- [ ] ✅ Listagem de pacientes (com paginação)
- [ ] ✅ Busca por CPF
- [ ] ✅ Seleção de paciente salva UUID

#### Questionários
- [ ] ✅ Carregar estrutura do IVCF-20
- [ ] ✅ Submeter respostas do questionário
- [ ] ✅ Buscar histórico de respostas
- [ ] ✅ Ver detalhes de resposta específica

#### Testes/Avaliações
- [ ] ✅ Listar health units
- [ ] ✅ Submeter teste 30-STS (com sensor data)
- [ ] ✅ Listar avaliações de um participante
- [ ] ✅ Verificar que `sensorData` é enviado corretamente

#### Profissional
- [ ] ✅ Cadastro de profissional
- [ ] ✅ Atualização de dados

### 3.3. Validação de Logs
- [ ] Executar `adb logcat` durante testes
- [ ] **Confirmação crítica: NENHUM `ClassCastException`**
- [ ] **Confirmação crítica: NENHUM `JsonSyntaxException`**
- [ ] Nenhum erro de deserialização Gson
- [ ] Nenhum erro de tipo genérico

---

## 4. Upload para Play Store (Internal Testing)

### 4.1. Criar App Bundle
- [ ] Executar `./gradlew bundleRelease`
- [ ] Verificar AAB em `app/build/outputs/bundle/release/app-release.aab`
- [ ] Confirmar assinatura digital do AAB

### 4.2. Upload para Internal Track
- [ ] Fazer upload do AAB para track de teste interno
- [ ] Adicionar release notes descrevendo as correções
- [ ] Aguardar aprovação do Google Play (geralmente < 1 hora)

### 4.3. Teste via Play Store
- [ ] Instalar app **via Play Store** (não via APK manual)
- [ ] Repetir **todos os testes da seção 3.2**
- [ ] Confirmar que não há ClassCastException
- [ ] Verificar logs via Firebase Crashlytics (se configurado)

---

## 5. Testes de Regressão (Build Production)

### 5.1. Fluxos Críticos
- [ ] ✅ **Login → Cadastro de Paciente → Teste 30-STS → Submissão**
- [ ] ✅ **Login → Listar Pacientes → Selecionar → Questionário IVCF-20**
- [ ] ✅ **Login → Listar Avaliações → Ver Detalhes**
- [ ] ✅ **Login Participante → Ver Histórico de Respostas**

### 5.2. Edge Cases
- [ ] Teste com conexão lenta (simular via Dev Tools)
- [ ] Teste com token expirado (forçar logout e re-login)
- [ ] Teste com dados de sensor vazios
- [ ] Teste com paciente sem histórico

---

## 6. Validação de Performance

### 6.1. Tempo de Resposta
- [ ] Login completa em < 3 segundos
- [ ] Listagem de pacientes em < 2 segundos
- [ ] Submissão de teste em < 5 segundos
- [ ] Questionário carrega em < 2 segundos

### 6.2. Uso de Memória
- [ ] App não consome > 200MB de RAM em operação normal
- [ ] Nenhum memory leak detectado (usar Android Studio Profiler)

---

## 7. Checklist Final Antes do Upload

- [ ] ✅ Todos os itens da seção 1 (Build) passaram
- [ ] ✅ Todos os itens da seção 2 (Código) passaram
- [ ] ✅ Todos os itens da seção 3 (Testes) passaram
- [ ] ✅ **ZERO ClassCastException em logs**
- [ ] ✅ **ZERO JsonSyntaxException em logs**
- [ ] ✅ ProGuard mapping salvo para rastreamento
- [ ] ✅ Release notes escritas

---

## 8. Pós-Upload (Monitoramento)

### 8.1. Play Console
- [ ] Verificar taxa de crashes em Play Console
- [ ] Confirmar que taxa de crashes < 1%
- [ ] Verificar que não há relatórios de ClassCastException

### 8.2. Firebase (se configurado)
- [ ] Monitorar Crashlytics por 24-48h
- [ ] Verificar stack traces de novos crashes
- [ ] Confirmar que correções resolveram o problema

---

## ✅ Aprovação Final

**Data:** ___________  
**Responsável:** ___________  
**Versão AAB:** ___________  

**Assinatura:** Confirmo que todos os itens críticos foram verificados e o AAB está pronto para produção.

---

## 📝 Notas de Correção

### O que foi corrigido nesta versão:
1. ✅ Substituído `List<Map<String, Any>>` por `List<SensorDataPoint>` em `Teste.kt`
2. ✅ Adicionado `@SerializedName` em todos os DTOs
3. ✅ Reforçadas regras ProGuard para preservar tipos genéricos
4. ✅ Refatorado `Timer.kt` para usar DTO fortemente tipado
5. ✅ Adicionadas regras específicas para todos os models no ProGuard

### Problema raiz identificado:
- `Map<String, Any>` perde informações de tipo durante compilação R8/ProGuard
- Gson não consegue deserializar corretamente em builds de produção
- Resultado: `ClassCastException` ao acessar `response.body()`

### Solução implementada:
- DTOs fortemente tipados com `@SerializedName`
- ProGuard configurado para preservar `Signature` e `*Annotation*`
- Zero uso de tipos genéricos inseguros (`Any`, `Map<String, Any>`)
