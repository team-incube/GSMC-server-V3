#!/bin/bash
set -ex

ECR_REGISTRY="588738598492.dkr.ecr.ap-northeast-2.amazonaws.com"
ECR_REPOSITORY_NAME="gsmc-ecr"
IMAGE_TAG="latest"
APP_NAME="gsmc-application"

aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin $ECR_REGISTRY

docker pull $ECR_REGISTRY/$ECR_REPOSITORY_NAME:$IMAGE_TAG

docker run -d \
  --name $APP_NAME \
  -p 8080:8080 \
  --restart unless-stopped \
  $ECR_REGISTRY/$ECR_REPOSITORY_NAME:$IMAGE_TAG

# backend 실행 후 nginx reload
sudo systemctl reload nginx
