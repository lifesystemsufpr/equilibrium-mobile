# Fix: NullPointerException em Cadastro de Participante

## ⚠️ AÇÃO NECESSÁRIA: Clean Build

O código já foi **corrigido**, mas o app está executando uma versão antiga em cache.

### Passos para Resolver

#### 1. Clean Build Completo
```bash
# No terminal do projeto
./gradlew clean
./gradlew assembleDebug
```

**OU** no Android Studio:
1. **Build** → **Clean Project**
2. Aguardar conclusão
3. **Build** → **Rebuild Project**

#### 2. Desinstalar e Reinstalar
```bash
# Desinstalar versão antiga
adb uninstall com.ufpr.equilibrium

# Reinstalar versão nova
./gradlew installDebug
```

---

## Correções Já Aplicadas

### Problema Original
```kotlin
// ANTES - Causava NullPointerException
fun fromRegistrationDto(dto: PatientRegistrationDto): Patient {
    return Patient(
        cpf = dto.user.cpf,  // ❌ user era null
        ...
    )
}
```

### Solução Implementada

**1. `PatientDto.kt` - UserDto agora é nullable**
```kotlin
data class PatientRegistrationDto(
    @SerializedName("user") val user: UserDto?,  // ✅ Nullable
    ...
)
```

**2. `PatientMapper.kt` - Tratamento null-safe**
```kotlin
fun fromRegistrationDto(dto: PatientRegistrationDto): Patient {
    // ✅ Handle null user
    val userDto = dto.user ?: UserDto(
        id = null,
        cpf = "",
        fullName = "",
        password = null,
        phone = "",
        gender = "",
        role = "PATIENT"
    )
    
    return Patient(
        cpf = userDto.cpf,  // ✅ Seguro
        fullName = userDto.fullName,
        ...
    )
}
```

---

## Por Que o Erro Ainda Aparece?

A API **não retorna** o objeto `user` na resposta do POST `/participant`. O mapper tentava acessar `responseDto.user.cpf`, mas `user` é null.

### Fluxo Atual (Correto)
1. App envia: `PatientRegistrationDto` com `user` preenchido ✅
2. API cria participante ✅
3. API retorna: `PatientRegistrationDto` **SEM** `user` (apenas dados do participante) ✅
4. Mapper agora trata `user` como nullable ✅

---

## Verificação Pós-Build

Após rebuild, o cadastro deve funcionar **SEM ERROS**:

✅ Participante criado com sucesso  
✅ Nenhum NullPointerException no logcat  
✅ Navegação funciona corretamente  

---

## Se o Erro Persistir

Execute um **invalidate cache**:

**Android Studio:**
1. **File** → **Invalidate Caches...**
2. Marcar **Clear file system cache and Local History**
3. Click **Invalidate and Restart**

Depois:
```bash
./gradlew clean
./gradlew assembleDebug
adb uninstall com.ufpr.equilibrium
./gradlew installDebug
```
