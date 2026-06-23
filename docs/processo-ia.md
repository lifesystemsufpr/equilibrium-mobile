# processo-IA — equilibrium-mobile

> Como aplicar **testes com IA, code review com IA e automação** neste repositório.
> Documento de orientação (o "como"). **Não contém código.** Task: [86e1tmk1q](https://app.clickup.com/t/86e1tmk1q).

## 1. Contexto do repo

- **Stack:** Kotlin / Android (Gradle 8.7, Kotlin 2.0, Clean Architecture modular: `app` / `domain` / `data` / `core-common`).
- **Teste hoje:** **JUnit 4** + **MockK** + **MockWebServer** + Espresso configurados; **~3 testes**
  (2 templates `ExampleUnitTest`/`ExampleInstrumentedTest` + `AuthRepositoryImplTest`).
- **Qualidade:** **detekt** e **ktlint** habilitados (mas `detekt maxIssues: 9999`).
- **CI:** **já existe** `.github/workflows/ci.yml` (assembleDebug, testDebugUnitTest, lintDebug, detekt, ktlintCheck).
- **Maturidade:** o repo **mais maduro** em tooling — aqui o foco é **ampliar cobertura**, não montar fundação.

## 2. Testes unitários com IA

Framework: **JUnit + MockK** (regra [`60-testing`](https://github.com/lifesystemsufpr/ai-toolkit/blob/main/source/rules/60-testing.md) recomenda
**JUnit5 + MockK** — ver nota). Gerar via [`generate-tests`](https://github.com/lifesystemsufpr/ai-toolkit/blob/main/source/skills/generate-tests.md)
(AAA + adversarial), priorizando a lógica pura das camadas internas.

**Alvos prioritários:**

| Prioridade | Alvo | Como testar |
|---|---|---|
| 1 | `domain/` (use cases) | Kotlin puro, sem framework — regras de negócio; entradas de fronteira. |
| 2 | `core-common/` (`Result`, mapeamento de erro) | Sucesso/erro; mapeamento de exceção → estado de erro. |
| 3 | `data/` (repositórios, ex.: `AuthRepositoryImpl`) | **MockWebServer** já em uso; respostas 2xx/4xx/5xx, payload inválido, parse de token. |
| 4 | mappers/DTOs de rede (`network/*`) | Mapeamento API→domínio; campos nulos/ausentes. |

> **Nota (documentar):** a regra `60-testing` pede **JUnit5**; o repo está em **JUnit4**. Migrar é um
> ajuste de tooling de teste (PR próprio, com OK) — ou registrar a exceção documentada de manter JUnit4.

**Cobertura:** alvo mobile/kotlin = **30%** ([`repos.config.ts`](https://github.com/lifesystemsufpr/devops-hub/blob/main/scripts/repos.config.ts)).
Para medir, documentar a adição de **JaCoCo** (`jacocoTestReport`) ao Gradle (o `ci-kotlin.yml` espera esse relatório).

## 3. Code review com IA

- [`review-pr`](https://github.com/lifesystemsufpr/ai-toolkit/blob/main/source/skills/review-pr.md) + regra
  [`75-code-review`](https://github.com/lifesystemsufpr/ai-toolkit/blob/main/source/rules/75-code-review.md), incluindo a regra Kotlin (`50-kotlin`).
- A IA deve sinalizar a flag de segurança conhecida: **senha inicial derivada da data de nascimento**
  (`feature_professional/EnderecoFragment.kt`) e **token não criptografado** no `SessionManager`.
- **Revisão humana obrigatória:** auth, schema (Room) e a coleta de **dados de sensor/avaliação** →
  [`review-clinical-change`](https://github.com/lifesystemsufpr/ai-toolkit/blob/main/source/skills/review-clinical-change.md).

## 4. Automação / CI

- Workflow reutilizável: **`ci-kotlin.yml`** (o repo já tem um `ci.yml` próprio — **alinhar/consolidar**
  com o reutilizável do `devops-hub` quando bootstrap rodar, com OK, evitando duplicar gates).
- Considerar baixar **`detekt maxIssues`** para um número realista (ajuste de config de qualidade, PR próprio)
  para o gate de fato pegar problemas.
- **Gate pré-PR local:** rodar `./gradlew testDebugUnitTest detekt ktlintCheck` antes do PR (espírito do
  [`pre-pr-gate`](https://github.com/lifesystemsufpr/ai-toolkit/blob/main/source/skills/pre-pr-gate.md), adaptado ao Gradle).

## 4b. Validação de runtime (mobile)

nav-check de **navegador não se aplica** (app Android nativo). O análogo é **Espresso** (instrumented
tests, **já presente** no repo) e, opcionalmente, **Maestro** para fluxos ponta a ponta — mesma filosofia
das [validações automáticas](https://github.com/lifesystemsufpr/devops-hub/blob/main/docs/processo-ia/validacoes-automaticas.md): gate no CI + varredura exploratória por IA.
Ampliar a partir dos `androidTest` existentes.

## 5. Guard-rails específicos (saúde / segurança)

- Coleta de **dados de sensor e avaliações de mobilidade** = dado de saúde → criptografia, sem dado real em teste.
- **Senha = data de nascimento** e **token em estático não criptografado**: itens de **review** (não corrigir nesta task de docs).
- Firebase Analytics ligado → revisar política de retenção/privacidade (fora desta task).

## 6. Passo a passo "como fazer"

1. Gerar testes com `generate-tests` nas camadas `domain` e `core-common` (alvos 1–2).
2. Ampliar testes de `data/` usando o padrão MockWebServer já existente.
3. Documentar adição de **JaCoCo** para medir cobertura contra o gate 30%.
4. Documentar decisão JUnit4→JUnit5 (migrar ou registrar exceção).
5. Rodar `./gradlew testDebugUnitTest detekt ktlintCheck` localmente → PR → `review-pr`.
6. Alinhar o `ci.yml` próprio com o `ci-kotlin.yml` do hub (com OK).
7. **Merge só com OK humano.**
