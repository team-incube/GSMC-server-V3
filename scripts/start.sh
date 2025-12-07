#!/bin/bash
set -e

ECR_REGISTRY="PLACEHOLDER"
ECR_REPOSITORY="gsmc-ecr"
IMAGE_TAG="latest"

echo "Logging in to ECR..."
aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin $ECR_REGISTRY

echo "Pulling latest image..."
docker pull $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

echo "Starting container..."
docker run -d \
  --name gsmc-app \
  -p 8080:8080 \
  --restart unless-stopped \
  $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

echo "Container started successfully"