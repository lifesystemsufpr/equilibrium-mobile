# 🔧 Correção: Seleções Sendo Desmarcadas no Questionário

## 🐛 Problema Identificado

**Sintoma:** Ao selecionar uma opção em uma questão, as opções de outras questões eram desmarcadas.

**Causa:** Ordem incorreta de operações no `onBindViewHolder`:
1. Listener era configurado ANTES de restaurar o estado
2. Ao restaurar seleção, o listener era disparado
3. O evento acionava `onAnswerChanged` de forma incorreta
4. Estado era sobrescrito/perdido

## ✅ Solução Implementada

### Mudança na Ordem de Operações:

**ANTES (Incorreto):**
```kotlin
// 1. Criar RadioButtons
holder.rgOptions.removeAllViews()
options.forEach { addRadioButton() }

// 2. Configurar listener ❌ (MUITO CEDO!)
holder.rgOptions.setOnCheckedChangeListener { ... }

// 3. Restaurar seleção
rb.isChecked = true  // ❌ Dispara o listener!
```

**DEPOIS (Correto):**
```kotlin
// 1. Remover listener antigo
holder.rgOptions.setOnCheckedChangeListener(null) // ✅

// 2. Criar RadioButtons
holder.rgOptions.removeAllViews()
options.forEach { addRadioButton() }

// 3. Restaurar seleção SEM listener
rb.isChecked = true  // ✅ Não dispara nada!

// 4. AGORA configurar listener
holder.rgOptions.setOnCheckedChangeListener { ... } // ✅
```

## 📋 Alterações Específicas

### 1. RadioGroup (Opções de Resposta)

```kotlin
// ✅ ANTES de manipular: remover listener
holder.rgOptions.setOnCheckedChangeListener(null)

// Limpar e recriar views
holder.rgOptions.removeAllViews()
// ... adicionar RadioButtons ...

// ✅ Restaurar estado (sem listener ativo)
val prevSelected = selectedIndices[q.id] ?: -1
if (prevSelected >= 0) {
    val rb = holder.rgOptions.getChildAt(prevSelected)
    rb.isChecked = true  // Não dispara evento
}

// ✅ DEPOIS: configurar listener
holder.rgOptions.setOnCheckedChangeListener { group, checkedId ->
    // Agora sim processa mudanças do usuário
}
```

### 2. EditText (Campo de Nota)

Mesmo problema e mesma solução:

```kotlin
// ✅ ANTES de setText: remover listener
holder.etNote.doAfterTextChanged(null)

// Restaurar texto
holder.etNote.setText(notes[q.id] ?: "")

// ✅ DEPOIS: configurar listener
holder.etNote.doAfterTextChanged { text ->
    // Processa mudanças do usuário
}
```

## 🔍 Por Que Isso Acontecia?

### RecyclerView Reusa Views

Quando você rola a lista:
1. View da questão 1 pode ser reusada para questão 6
2. View vem com listener ainda ativo da questão 1
3. Ao configurar questão 6:
   - Listener da questão 1 ainda ativo
   - `clearCheck()` dispara listener
   - Evento processado como se fosse usuário
   - Estado incorreto salvo

### Solução: Limpar Estado Antes de Re-configurar

```kotlin
// Sempre nesta ordem:
1. Remover listeners antigos        // ✅ Desativa eventos
2. Modificar views                  // ✅ Sem side effects
3. Restaurar estado salvo           // ✅ Sem disparar eventos
4. Configurar novos listeners       // ✅ Pronto para usuário
```

## ✅ Comportamento Correto Agora

### Teste 1: Responder Questão 1
```
Usuário seleciona opção 2 na questão 1
→ selectedIndices[1] = 2  ✅
→ Listener dispara
→ onAnswerChanged(1, 2, score, note) ✅
```

### Teste 2: Rolar Para Questão 5
```
RecyclerView reusa view da questão 1 para mostrar questão 5
→ setOnCheckedChangeListener(null)  ✅ Remove listener
→ removeAllViews()                   ✅ Limpa opções
→ Cria opções da questão 5          ✅
→ Restaura estado: selectedIndices[5]  ✅
→ Configura novo listener            ✅
→ Questão 1 mantém selectedIndices[1] = 2  ✅ Preservado!
```

### Teste 3: Voltar Para Questão 1
```
RecyclerView mostra questão 1 de novo
→ setOnCheckedChangeListener(null)  ✅
→ removeAllViews()                   ✅
→ Cria opções da questão 1          ✅
→ Restaura: selectedIndices[1] = 2  ✅ Estado recuperado!
→ rb.isChecked = true               ✅ Visual restaurado
→ Configura listener                ✅
```

## 🎯 Estado Mantido em `selectedIndices`

```kotlin
// Mapa persiste estado de TODAS as questões
private val selectedIndices = mutableMapOf<Int, Int>().apply {
    questions.forEach { put(it.id, -1) }
}

// Questão 1: opção 2 selecionada
selectedIndices[1] = 2  ✅

// Questão 2: nenhuma seleção
selectedIndices[2] = -1  ✅

// Questão 3: opção 0 selecionada
selectedIndices[3] = 0  ✅
```

## 📊 Fluxo Completo Corrigido

```
Usuário seleciona opção
         ↓
Listener dispara setOnCheckedChangeListener
         ↓
Salva em selectedIndices[questionId] = index  ✅
         ↓
Chama onAnswerChanged(questionId, index, score, note)
         ↓
ViewModel recebe e armazena
         ↓
Usuário rola a lista
         ↓
onBindViewHolder é chamado para questão diferente
         ↓
Remove listener antigo  ✅
         ↓
Restaura estado de selectedIndices  ✅
         ↓
Configura novo listener  ✅
         ↓
Estado anterior preservado!  ✅
```

## ✅ Verificação

### Checklist de Funcionamento:

- [ ] Selecionar opção em questão 1 → Marca opção ✅
- [ ] Rolar para baixo → Questão 1 mantém seleção ✅
- [ ] Selecionar opção em questão 5 → Marca opção ✅
- [ ] Rolar para cima → Questão 1 AINDA marcada ✅
- [ ] Questão 5 AINDA marcada ✅
- [ ] Todas as questões independentes ✅

## 💡 Lição Aprendida

**Sempre ao trabalhar com RecyclerView:**

1. **Limpe listeners** antes de modificar views
2. **Restaure estado** antes de configurar listeners
3. **Configure listeners** apenas no final
4. **Mantenha estado** em estrutura separada (não na View)

---

**Status:** ✅ Corrigido  
**Arquivo:** QuestionnaireAdapter.kt  
**Problema:** Listeners disparando durante restauração de estado  
**Solução:** Remover → Restaurar → Configurar
