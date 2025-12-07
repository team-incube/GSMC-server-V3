#!/bin/bash
APP_NAME="gsmc-app"

docker stop $APP_NAME || true
docker rm $APP_NAME || true