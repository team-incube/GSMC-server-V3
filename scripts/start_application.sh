#!/bin/bash

IMAGE_NAME="gsmc-server"
CONTAINER_NAME="gsmc-app"

echo "> Starting application deployment..."
echo "> Building Docker image: $IMAGE_NAME"
docker build -t $IMAGE_NAME -f DockerfileProd .
if [ $? -eq 0 ]; then
    echo "> Docker image built successfully"
else
    echo "> Failed to build Docker image"
    exit 1
fi

echo "> Starting Docker container: $CONTAINER_NAME"
docker run -d \
    --name $CONTAINER_NAME \
    --add-host=host.docker.internal:host-gateway \
    -e RDB_HOST=host.docker.internal \
    -e REDIS_HOST=host.docker.internal \
    -p 8080:8080 \
    $IMAGE_NAME

if [ $? -eq 0 ]; then
    echo "> Container started successfully"
    echo "> Application is running on port 8080"
else
    echo "> Failed to start container"
    exit 1
fi
