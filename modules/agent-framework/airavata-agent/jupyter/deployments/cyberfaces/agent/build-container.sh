cd ../../../../
env GOOS=linux GOARCH=amd64 go build
cp airavata-agent jupyter/deployments/cyberfaces/agent/airavata-agent-linux
cd jupyter/deployments/cyberfaces/agent
docker build --platform linux/x86_64 -t cybershuttle/remote-agent-cyberfaces .
docker push cybershuttle/remote-agent-cyberfaces