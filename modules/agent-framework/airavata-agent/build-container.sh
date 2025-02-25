env GOOS=linux GOARCH=amd64 go build -o airavata-agent
docker build --platform linux/x86_64 -t cybershuttle/remote-agent-base .
docker push cybershuttle/remote-agent-base