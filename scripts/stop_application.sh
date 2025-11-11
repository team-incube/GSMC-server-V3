#!/bin/bash

IMAGE_NAME="gsmc-server"
CONTAINER_NAME="gsmc-app"

echo "> Stopping Docker container: $CONTAINER_NAME"
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
    echo "> Stopping running container..."
    docker stop $CONTAINER_NAME
    echo "> Container stopped"

else
    echo "> No running container found"
fi

if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "> Removing container..."
    docker rm $CONTAINER_NAME
    echo "> Container removed"

else
    echo "> No container to remove"
