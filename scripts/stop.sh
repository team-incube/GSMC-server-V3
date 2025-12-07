#!/bin/bash
set -ex

APP_NAME="gsmc-application"

CONTAINER_ID=$(docker ps -aq --filter "name=$APP_NAME")

if [ -n "$CONTAINER_ID" ]; then
    docker stop $CONTAINER_ID
    docker rm $CONTAINER_ID
fi
