#!/usr/bin/env bash

RABBITMQ_DOWNLOAD_URL="https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_5_1/rabbitmq-server-generic-unix-3.5.1.tar.gz"
RABBITMQ_DIRECTORY="rabbitmq_server-3.5.1"

echo "Downloading RabbitMQ Distribution from $RABBITMQ_DOWNLOAD_URL"

curl -L $RABBITMQ_DOWNLOAD_URL | tar xz

echo "Changing into RabbitMQ Directory $RABBITMQ_DIRECTORY"
cd $RABBITMQ_DIRECTORY

if [ -f `pwd`/$RABBITMQ_DIRECTORY/sbin/rabbitmq-server ]; then
   echo "RabbitMQ Server script exists"
else
   echo "RabbitMQ startup script does not exist. Please check the download"
fi





