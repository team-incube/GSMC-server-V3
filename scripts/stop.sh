#!/bin/bash
APP_NAME="gsmc-application" 
docker stop $APP_NAME || true
docker rm $APP_NAME || true