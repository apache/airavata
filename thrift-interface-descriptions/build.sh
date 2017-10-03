#!/bin/bash

sudo apt-get install jq

access_token=`curl --data "username=$1&password=$2" https://dev.testdrive.airavata.org/api-login | jq '.access_token'`

sed -i -e "s/token999/$access_token/g" client.go

go run *.go
