#!/bin/bash

echo "--- Updating Zap.Yaml for CI --"
sed -i 's|zap-results|tmp|g' zap.yaml

# Change to repo root so docker compose has correct context
cd ../../../

docker compose \
    -f $(pwd)/deploy/scripts/zap/docker-compose.yml \
    up \
    --exit-code-from zap-scan