#!/bin/bash
set -e

LOG=/home/ec2-user/application/deploy.log
echo "[$(date --iso-8601=seconds)] docker health-check start" | tee -a $LOG

sleep 5

if curl -sfS http://localhost:8080/api/v3/health > /dev/null; then
  echo "[$(date --iso-8601=seconds)] health-check OK" | tee -a $LOG
  exit 0
else
  echo "[$(date --iso-8601=seconds)] health-check FAILED" | tee -a $LOG
  exit 1
fi
