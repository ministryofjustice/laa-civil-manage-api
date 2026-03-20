#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
RESULTS_DIR="${REPO_ROOT}/zap-results"
REPORT_PATH="${RESULTS_DIR}/zap-baseline-report.html"

echo "--- Preparing Report Directory ---"
mkdir -p "${RESULTS_DIR}"

echo "--- Cleaning Report ---"
rm -f "${REPORT_PATH}" "${RESULTS_DIR}/zap-baseline-report.json"

docker compose \
    -f "${SCRIPT_DIR}/docker-compose.yml" \
    up \
    --remove-orphans \
    --exit-code-from zap-scan

echo "--- Opening Report ---"
open "${REPORT_PATH}"