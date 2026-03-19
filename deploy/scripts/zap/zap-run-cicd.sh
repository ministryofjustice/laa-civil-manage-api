#!/bin/bash

echo "--- Updating Zap.Yaml for CI --"
sed -i 's|zap-results|tmp|g' zap.yaml


docker compose \
    -f ./docker-compose.yml \
    up \
    --exit-code-from zap-scan