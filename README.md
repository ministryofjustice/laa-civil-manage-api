# LAA Civil Manage API

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-civil-manage-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/laa-civil-manage-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://laa-civil-manage-api-dev.cloud-platform.service.justice.gov.uk/swagger-ui/index.html)

API to allow legal providers to manage their applications for civil legal aid.

## Local development

Requires Java 25 (managed by `mise.toml`).

This repo uses Spotless + Lefthook for local quality gates:

```bash
mise install
lefthook install
```

Configured hooks:
- pre-commit: `env -u JAVA_HOME ./gradlew spotlessApply spotlessCheck`
- pre-push: `env -u JAVA_HOME ./gradlew test`

The hooks explicitly unset `JAVA_HOME` to avoid failures in GUI Git clients that export `JAVA_HOME=undefined`.

```bash
./gradlew build                  # full build + tests
./gradlew test                   # run tests only
./gradlew test --rerun-tasks     # ignore cached results
./gradlew bootRun --args='--spring.profiles.active=local' # run locally with clean logging
./gradlew generateOpenApiDocs    # regenerate openApi/*.json
./gradlew spotlessApply          # auto-format code/config/docs
./gradlew spotlessCheck          # verify formatting
```
> While running locally, you can view the API docs at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).

Run a single test class:

```bash
./gradlew test --tests "uk.gov.justice.laa_civil_manage_api.controllers.PriorAuthorityControllerTest"
```

## Example requests

All examples assume a local instance running at `http://localhost:8080`.

### Submit a prior-authority request

```bash
curl -i -X POST http://localhost:8080/prior-authority \
  -H 'Content-Type: application/json' \
  -d '{
    "applicationId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "priorAuthorityType": "EXPERT",
    "expertType": "Psychologist",
    "expertFullName": "Dr John Doe",
    "expertPostcode": "SW1H 9AJ",
    "expertBasedInLondon": true,
    "billingType": "FIXED_RATE",
    "totalAmount": 249.99,
    "justification": "Specialist evidence is required to progress the case."
  }'
```

### Prior-authority drafts

Drafts let users save a partially-completed prior-authority form and come back later.

#### Create a draft

```bash
curl -i -X POST http://localhost:8080/prior-authority/drafts \
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
curl -i http://localhost:8080/prior-authority/drafts/c3b07e24-d92b-410a-9d95-88f117a12b43
```

#### List the current user's drafts

```bash
curl -i http://localhost:8080/prior-authority/drafts
curl -i 'http://localhost:8080/prior-authority/drafts?applicationId=2a28f60d-fe15-43fe-92c3-5530595d5f51'
```

#### Delete a draft

```bash
curl -i -X DELETE http://localhost:8080/prior-authority/drafts/c3b07e24-d92b-410a-9d95-88f117a12b43
```
