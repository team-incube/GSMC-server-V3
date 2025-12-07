#!/bin/bash
echo "Waiting for application to start..."
sleep 15

HEALTH_URL="https://api.gsmc.io.kr/api/v3/health"

echo "Checking health at $HEALTH_URL..."
if curl -f $HEALTH_URL; then
  echo "Health check passed!"
  exit 0
else
  echo "Health check failed!"
  exit 1
if