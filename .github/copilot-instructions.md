# GitHub Copilot Instructions — laa-civil-manage-api

## 1. Before Starting Any Feature

1. **Ask for the Jira ticket ID** (`CM-XXX`) if not provided. Name the branch
   `CM-XXX-short-concise-description` (hyphen-separated, no unnecessarily long names).
2. **Clarify any ambiguous requirements** before writing code.
3. **Confirm a clean baseline**: Run `mise test` and `mise format` before starting edits.
4. **Find the nearest analogous existing feature** (e.g. `PriorAuthorityController` +
   `PriorAuthorityService` + `services/accessdatastore/AccessDataStoreClient`) and follow the same
   pattern rather than inventing a new one.

## 2. Workflow — Macro-Cycle TDD (Test-First, Plan, Execute)

We strictly follow a Test-Driven Development (TDD) philosophy adapted for AI Agent workflows (**Macro-Cycle TDD**). For any new feature or non-trivial change, do NOT write implementation and test code simultaneously in a single turn. Follow this two-phase contract workflow:

### Phase 1: The Contract (Red Phase / Planning)
1. Acknowledge the requirement, list affected files, and outline the feature design.
2. Write the **tests first**. Provide the complete test classes, methods, and assertions that define the required behavior.
3. **STOP.** Ask the user explicitly: *"Does this test coverage and plan look correct?"* Do not output or write implementation code until approved.

### Phase 2: Implementation & Validation (Green Phase)
1. Once the user approves Phase 1 (or provides corrections), write the minimal production code needed to satisfy the tests.
2. **Execute Validation Commands:** Run the required terminal checks to verify compilation, test passing, and formatting:
    - `mise format`
    - `mise test`
    - `mise coverage`
3. **Refactor & Self-Correct:** If tests or formatting checks fail, automatically fix the errors and re-run checks until green.
4. **Surgical changes only:** When modifying existing files, output only the updated methods or code blocks rather than rewriting entire files, unless requested.

### Execution Guardrails
- **NEVER install a new dependency** (Gradle or otherwise) without checking with the user first — recommend it instead.
- **Keep business logic out of controllers.** Controllers only validate/parse input, call a service, and shape the `ResponseEntity`. Services hold business logic and call downstream clients (`services/accessdatastore`, `services/legalframework`, `services/providerdetails`) — never call an `HttpClient`/`RestClient` directly from a controller.
- **No change should reduce test coverage.** Compare test results against gates in [§6](#6-code-coverage).
- **When finished**, run `mise apidocs` if a controller or model changed, and verify OpenAPI sync via `./gradlew verifyOpenApiSync`.

### Terminal Commands Reference

```bash
mise format     # Formatting (./gradlew spotlessApply spotlessCheck)
mise test       # Unit + integration tests (JUnit 5)
mise coverage   # Coverage report (./gradlew test jacocoTestReport, build/reports/jacoco/)
mise apidocs    # Regenerate OpenAPI spec (./gradlew generateOpenApiDocs)
./gradlew verifyOpenApiSync       # Fails if openApi/openApi.json is stale (no mise task yet)
```

### When editing existing files

- Make surgical changes only. Do not refactor unrelated code.
- Do not change test assertions without understanding why they were written that way.
- Fix Spotless failures with `mise format` — do not hand-format or suppress the check.

If these instructions do not cover a specific case, stop and ask.

## 3. Architecture Rules

This is a **Java 25 + Spring Boot 4.1 + Gradle (Kotlin DSL)** REST API, base package
`uk.gov.justice.laa_civil_manage_api`, with a separate `notify-integration` Gradle subproject
(`uk.gov.justice.laa.civil.notify`) for GOV.UK Notify email sending.

- **`config/`** — `@Configuration` classes: `SecurityConfig` (Entra ID OAuth2 resource server + OBO
  client), `RestClientConfig`, `NotifyConfig`/`NotifyEmailProperties`, `OpenApiConfig`.
- **`controllers/`** — `@RestController`s (`PriorAuthorityController`, `ApplicationsController`,
  `ExpertTypeController`) plus `RestClientErrorHandler`. Request/response only — no business logic.
- **`services/`** — business logic (`PriorAuthorityService`, `ApplicationsService`,
  `ExpertTypeService`, `ProviderDetailsService`), one per resource/feature.
- **`services/accessdatastore/`**, **`services/legalframework/`**, **`services/providerdetails/`** —
  per-downstream-system clients (interface + `Http*Client` impl), request/response DTOs, `*Properties`
  (`@ConfigurationProperties`), and health indicators. Services depend on the client *interface*, not
  the `Http*` implementation, so tests can mock it.
- **`models/`** — shared request/response/domain DTOs exposed via the API (used by controllers and
  services), distinct from the downstream-specific DTOs living under `services/<system>/`.
- **`logging/`** — correlation ID filter/interceptor for propagating a request ID to downstream calls.
- **`notify-integration/`** — standalone subproject wrapping the GOV.UK Notify Java client; depended on
  via `implementation(project(":notify-integration"))`.

### Adding a new endpoint/feature

1. **Phase 1 (Contract):** Write request/response models in `models/` (or `services/<system>/`) and define the unit/integration tests (`[Class]Test.java` / `[Feature]IntegrationTest.java`). Pause for user approval.
2. **Phase 2 (Implementation):**
    - Add/extend downstream client interface + `Http*Client` implementation under `services/<system>/`.
    - Add/extend service in `services/` applying business logic.
    - Add/extend `@RestController` in `controllers/` with `@Operation`/`@ApiResponses` OpenAPI annotations.
    - Run `mise test`, `mise format`, and `mise apidocs` to validate work and update the OpenAPI spec.

## 4. Coding Conventions

- Java 25, Lombok (`@RequiredArgsConstructor`, `@Slf4j`) for boilerplate — prefer constructor injection
  via Lombok over manual constructors or field injection.
- Formatting is enforced by Spotless (`googleJavaFormat` for `*.java`, `ktlint` for `*.gradle.kts`) — run `mise format` to fix failures.
- Controllers throw/translate errors via `ResponseStatusException` or `RestClientErrorHandler`; don't
  introduce a new exception-handling pattern without checking existing controllers first.

### Naming

| Thing                      | Convention                                               | Example                                                 |
| --------------------------- | ---------------------------------------------------------- | ---------------------------------------------------------- |
| Packages                    | lowercase, `snake_case` base package                     | `uk.gov.justice.laa_civil_manage_api.services`          |
| Classes/interfaces          | `PascalCase`                                             | `PriorAuthorityService`, `AccessDataStoreClient`        |
| Downstream client impl      | `Http[System]Client`                                     | `HttpLegalFrameworkClient`, `HttpAccessDataStoreClient` |
| `@ConfigurationProperties`  | `[System]Properties`                                     | `LegalFrameworkProperties`, `AccessDataStoreProperties` |
| URL paths                   | `kebab-case`, resource-plural                            | `/prior-authorities`, `/expert-types`                   |
| Unit test files             | `[Class]Test.java`, mirrors main package                 | `controllers/PriorAuthorityControllerTest.java`         |
| Integration test files      | `[Feature]IntegrationTest.java`, flat under base package | `PriorAuthorityIntegrationTest.java`                    |
| `*.gradle.kts` files        | ktlint-formatted Kotlin                                  | `build.gradle.kts`                                      |

> Note: integration tests currently live flat under `uk.gov.justice.laa_civil_manage_api` rather than
> mirroring the `src/main/java` package tree (unlike unit tests, which do mirror it). Follow this
> existing split — don't move integration tests into subpackages as a drive-by change.

## 5. Testing Standards

- **Unit tests**: JUnit 5 + Mockito. Controller tests use `@WebMvcTest` + `@MockitoBean` for the service
  layer (see `PriorAuthorityControllerTest`); service/client tests mock their collaborators directly
  with Mockito.
- **Integration tests**: `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)` combined with
  REST-assured's `spring-mock-mvc`/`RestClient` calls and a `WireMockExtension` per downstream system
  (`@DynamicPropertySource` overrides the relevant `*.base-url` property to point at WireMock).
  `@MockitoBean private JwtDecoder jwtDecoder;` is used to bypass real Entra token validation.
- No dedicated fixtures directory — stub payloads are built inline per test (JSON strings /
  `Map.of(...)` / builder calls on the model classes) next to the test that uses them.
- Run `mise test` to confirm clean builds before completing tasks.

## 6. Code Coverage

- Coverage is measured with JaCoCo (`mise coverage`, report at
  `build/reports/jacoco/jacoco.xml` / `html/index.html`).
- CI (`.github/workflows/deploy.yml`, `coverage` job) posts a PR comment via `madrapps/jacoco-report`
  and gates on `min-coverage-overall: 90` — an overall drop below 90% fails the PR check.
- `min-coverage-changed-files` is set to **80** so newly-introduced or materially-changed files in a PR are held to a coverage bar.
- Generate unit tests in Phase 1 for any new class — don't rely on the overall/changed-files thresholds passing by coincidence from unrelated existing tests.
- **Raise the thresholds as coverage improves** — don't lower them to make a failing PR pass.

## 7. Exploration

Output exploration notes and plans as a markdown file outside the repo (e.g. session workspace), not
committed to the repo.