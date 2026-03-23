#!/bin/bash

echo "--- Updating Zap.Yaml for CI --"
sed -i 's|zap-results|tmp|g' zap.yaml

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"

cd ../../../

docker compose \
    -f ${SCRIPT_DIR}/docker-compose.yml \
    up \
    --exit-code-from zap-scan