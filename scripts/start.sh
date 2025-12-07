#!/bin/bash
APP_DIR="/home/ec2-user/gsmc-application"

cd $APP_DIR

docker-compose pull
docker-compose up -d
