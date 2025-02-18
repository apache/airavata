cd ../../../../
env GOOS=linux GOARCH=amd64 go build
cp airavata-agent jupyter/deployments/i-guide/agent/airavata-agent-linux
cd jupyter/deployments/i-guide/agent
docker build --platform linux/x86_64 -t cybershuttle/airavata-iguide-agent .
docker push cybershuttle/airavata-iguide-agent