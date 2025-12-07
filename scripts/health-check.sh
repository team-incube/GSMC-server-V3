#!/bin/bash
echo "Waiting for application to start..."
sleep 15

HEALTH_URL="https://api.gsmc.io.kr/api/v3/health"
MAX_RETRIES=5
RETRY_INTERVAL=5

for i in $(seq 1 $MAX_RETRIES); do
  echo "Checking health at $HEALTH_URL (attempt $i/$MAX_RETRIES)..."
  if curl -f -s $HEALTH_URL; then
    echo "Health check passed!"
    exit 0
  fi
  
  if [ $i -lt $MAX_RETRIES ]; then
    echo "Health check failed, retrying in ${RETRY_INTERVAL}s..."
    sleep $RETRY_INTERVAL
  fi
done

echo "Health check failed after $MAX_RETRIES attempts!"
exit 1