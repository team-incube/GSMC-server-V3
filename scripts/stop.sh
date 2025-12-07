#!/bin/bash
APP_NAME="gsmc-application"
APP_DIR="/home/ec2-user/gsmc-application"

cd $APP_DIR

docker-compose down || true
docker stop $APP_NAME || true
docker rm $APP_NAME || true
