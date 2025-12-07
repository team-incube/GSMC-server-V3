#!/bin/bash
set -ex

HEALTH_URL="https://api.gsmc.io.kr/api/v3/health"
MAX_RETRIES=10
SLEEP_TIME=3

for i in $(seq 1 $MAX_RETRIES); do
    response=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)

    if [ "$response" = "200" ]; then
        exit 0
    fi
    sleep $SLEEP_TIME
done

exit 1