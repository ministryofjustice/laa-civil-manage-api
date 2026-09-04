# LAA Civil Manage API

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-civil-manage-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/laa-civil-manage-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://laa-civil-manage-api-dev.cloud-platform.service.justice.gov.uk/swagger-ui/index.html)

API to allow legal providers to manage their applications for civil legal aid.

## Local development

Requires Java 25 (managed by `mise.toml`).

This repo uses Spotless + Lefthook for local quality gates:

```bash
mise install
mise exec -- lefthook install
```

Configured hooks:

- pre-commit: `env -u JAVA_HOME ./gradlew spotlessApply spotlessCheck`
- pre-push: `env -u JAVA_HOME ./gradlew test`

The hooks explicitly unset `JAVA_HOME` to avoid failures in GUI Git clients that export `JAVA_HOME=undefined`.

Running with the `local` profile needs Entra/ADS secrets. Copy the template and fill it in — `.env`
is gitignored and loaded automatically (via `spring.config.import` in `application-local.yaml`):

```bash
cp .env.example .env             # then fill in the values; never commit .env
```

```bash
./gradlew build                  # full build + tests
./gradlew test                   # run tests only
./gradlew test --rerun-tasks     # ignore cached results
./gradlew bootRun --args='--spring.profiles.active=local' # run locally with clean logging
./gradlew generateOpenApiDocs    # regenerate openApi/*.json
./gradlew spotlessApply          # auto-format code/config/docs
./gradlew spotlessCheck          # verify formatting
```

> While running locally, you can view the API docs
> at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).

Run a single test class:

```bash
./gradlew test --tests "uk.gov.justice.laa_civil_manage_api.controllers.PriorAuthorityControllerTest"
```

### Code coverage

Coverage is measured with [JaCoCo](https://docs.gradle.org/current/userguide/jacoco_plugin.html). Generate a
report locally to check your coverage before raising a PR:

```bash
./gradlew test jacocoTestReport   # run tests and generate the coverage report
```

The report is written under `build/reports/jacoco/`:

- `jacoco.xml` — machine-readable report (consumed by CI)
- `html/index.html` — open in a browser for a line-by-line breakdown

On a pull request, CI runs this report and posts a coverage summary as a PR comment. The build gates on an
overall coverage threshold (`min-coverage-overall` in `.github/workflows/deploy.yml`), so a significant drop
will be flagged there.

## Authentication

This API is fully secured using Microsoft Entra ID via OAuth 2.0.

* **Frontend API Calls:** All incoming requests must be authenticated using the **Authorization Code flow**. The
  frontend application attaches a valid user JWT (Bearer token) to the `Authorization` header of every request.
* **Downstream API Calls:** Any request that needs to interact with the downstream Access Data Store utilizes the *
  *On-Behalf-Of (OBO) flow**. The backend exchanges the user's incoming Entra token for a new token scoped specifically
  for the Data Store, ensuring strict, end-to-end user identity propagation.

*(Note: If you need to test endpoints locally without a token, you can temporarily set `SKIP_AUTH=true` in your `.env`
file).*

## CORS

CORS requires an explicit allowlist of trusted frontend origins (no wildcards). It is configured centrally in `SecurityConfig`.

Set the comma-separated allowlist per environment via `CORS_ALLOWED_ORIGINS`:

```text
CORS_ALLOWED_ORIGINS=https://laa-civil-manage-dev.cloud-platform.service.justice.gov.uk
```

- Local dev: Defaults to http://localhost:3000,http://localhost:5173 (override in .env).
- Deployed envs: Set in deploy/infrastructure/helm/values-*.yaml. An empty string ("") acts as a fail-safe, denying all cross-origin requests.
- Enforcement: Browsers block unlisted origins; non-browser server-to-server calls are unaffected.
- Request headers: Allow * to support automatically injected APM/tracing headers (e.g., AWS X-Ray).
- Exposed headers: Location and X-Correlation-ID are explicitly exposed so frontend JS can read 201 Created responses and track request IDs.

## Example requests

All examples assume a local instance running at `http://localhost:8080`. Unless `SKIP_AUTH=true` is set locally, all
requests require a valid Entra ID token in the `Authorization` header.

### Submit a prior-authority request

Because a Prior Authority request can vary significantly based on its type, the payload relies on specific nested domains (expertDetails, counselDetails, or disbursementDetails) corresponding to the priorAuthorityType.

#### Expert

```bash
curl -i -X POST http://localhost:8080/prior-authority \
  -H "Authorization: Bearer <token>" \
  -H 'Content-Type: application/json' \
  -d '{
    "applicationId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "priorAuthorityType": "EXPERT",
    "justification": "Required for comprehensive child behavioral assessment. Costs split 4 ways.",
    "expertDetails": {
      "expertType": "Psychologist",
      "expertFullName": "Dr John Doe",
      "expertPostcode": "SW1H 9AJ",
      "expertCosts": {
        "billingType": "HOURLY",
        "hourlyRate": 50.00,
        "timeRequested": {
          "hours": 2,
          "minutes": 30
        },
        "totalAmount": 125.00,
        "costsSharedWithOtherParties": true,
        "apportionment": {
          "partiesSharingCosts": 4,
          "clientShareAmount": 31.25
        }
      }
    }
  }'
```

#### Counsel

```bash
curl -i -X POST http://localhost:8080/prior-authority \
  -H "Authorization: Bearer <token>" \
  -H 'Content-Type: application/json' \
  -d '{
    "applicationId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "priorAuthorityType": "COUNSEL",
    "justification": "Required highly specialized counsel for complex cross-jurisdictional elements.",
    "counselDetails": {
      "counselType": "KINGS_COUNSEL_ALONE"
    }
  }'
```

#### Disbursement

```bash
curl -i -X POST http://localhost:8080/prior-authority \
  -H "Authorization: Bearer <token>" \
  -H 'Content-Type: application/json' \
  -d '{
    "applicationId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "priorAuthorityType": "DISBURSEMENT",
    "justification": "Train fare required for expert to attend the client assessment in person.",
    "disbursementDetails": {
      "disbursementPurpose": "Travel",
      "disbursementAmount": 125.50
    }
  }'
```

### Upload a document

```bash
curl -X POST http://localhost:8080/prior-authority/documents \
  -F "file=@./example.jpg"
```

### Prior-authority drafts

Drafts let users save a partially-completed prior-authority form and come back later.

For `timeMinutes`, use values from `0` to `59`. If the time is longer, add to `timeHours` instead.

#### Create a draft

```bash
curl -i -X POST http://localhost:8080/prior-authority/drafts \
  -H "Authorization: Bearer <token>" \
  -H 'Content-Type: application/json' \
  -d '{
    "applicationId": "2a28f60d-fe15-43fe-92c3-5530595d5f51",
    "priorAuthorityType": "EXPERT",
    "expertType": "Child psychologist",
    "expertFullName": "Dr Joe Bloggs",
    "expertPostcode": "N1 9GU",
    "billingType": "HOURLY",
    "hourlyRate": 45.00,
    "timeHours": 3,
    "timeMinutes": 0,
    "totalAmount": 135.00,
    "justification": "Drafting expert report estimate."
  }'
```

Returns `201` with `{"draftId": "..."}`

#### Update an existing draft

```bash
curl -i -X PUT http://localhost:8080/prior-authority/drafts/c3b07e24-d92b-410a-9d95-88f117a12b43 \
  -H "Authorization: Bearer <token>" \
  -H 'Content-Type: application/json' \
  -d '{
    "applicationId": "2a28f60d-fe15-43fe-92c3-5530595d5f51",
    "priorAuthorityType": "EXPERT",
    "expertType": "Child psychologist",
    "expertFullName": "Dr Joe Bloggs",
    "billingType": "HOURLY",
    "hourlyRate": 45.00,
    "timeHours": 4,
    "timeMinutes": 0,
    "totalAmount": 180.00,
    "justification": "Updated estimate after case review."
  }'
```

#### Get a draft by ID

```bash
curl -i http://localhost:8080/prior-authority/drafts/c3b07e24-d92b-410a-9d95-88f117a12b43 \
  -H "Authorization: Bearer <token>"
```

#### List the current user's drafts

```bash
curl -i http://localhost:8080/prior-authority/drafts -H "Authorization: Bearer <token>"
curl -i 'http://localhost:8080/prior-authority/drafts?applicationId=2a28f60d-fe15-43fe-92c3-5530595d5f51' -H "Authorization: Bearer <token>"
```

#### Delete a draft

```bash
curl -i -X DELETE http://localhost:8080/prior-authority/drafts/c3b07e24-d92b-410a-9d95-88f117a12b43 \
  -H "Authorization: Bearer <token>"
```
