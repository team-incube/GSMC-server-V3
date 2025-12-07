#!/bin/bash
echo "Waiting for application to start..."
sleep 15

echo "Checking health..."
if curl -f http://localhost:8080/api/v3/health; then
  echo "Health check passed!"
  exit 0
else
  echo "Health check failed!"
  exit 1
fi