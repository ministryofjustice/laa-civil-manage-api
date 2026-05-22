# LAA Civil Manage API

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-civil-manage-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/laa-civil-manage-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://laa-civil-manage-api-dev.cloud-platform.service.justice.gov.uk/swagger-ui/index.html)

API to allow legal providers to manage their applications for civil legal aid.

## Local development

Requires Java 25 (managed by `mise.toml`).

```bash
./gradlew build                  # full build + tests
./gradlew test                   # run tests only
./gradlew test --rerun-tasks     # ignore cached results
./gradlew bootRun                # run locally on http://localhost:8080
./gradlew generateOpenApiDocs    # regenerate openApi/*.json
```

Run a single test class:

```bash
./gradlew test --tests "uk.gov.justice.laa_civil_manage_api.controllers.PriorAuthorityControllerTest"
```

## Example request

Submit a prior-authority request against a running local instance:

```bash
curl -i -X POST http://localhost:8080/prior-authority-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "applicationId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "type": "EXPERT",
    "expertType": "Psychologist",
    "expertFullName": "Dr John Doe",
    "guidelineRatesExceeded": false,
    "billingType": "FLAT_RATE",
    "flatRateTotalAmount": 249.99
  }'
```
