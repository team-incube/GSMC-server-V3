#!/bin/bash
set -ex

APP_DIR="/home/ec2-user/gsmc-application"
cd $APP_DIR

IMAGE_REGISTRY="588738598492.dkr.ecr.ap-northeast-2.amazonaws.com"
REPO_NAME="gsmc-ecr"
TAG="latest"
IMAGE_URI="$IMAGE_REGISTRY/$REPO_NAME:$TAG"

CONTAINER_NAME="gsmc-application"

aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin $IMAGE_REGISTRY

docker stop $CONTAINER_NAME || true
docker rm $CONTAINER_NAME || true

docker pull $IMAGE_URI

docker run -d \
  --name $CONTAINER_NAME \
  -p 8080:8080 \
  --restart unless-stopped \
  --env-file $APP_DIR/.env \
  -e SPRING_PROFILES_ACTIVE=prod \
  $IMAGE_URI
