IMAGE_NAME=cybershuttle/remote-agent-cerebrum
AGENT_SVC_URL=api.gateway.cybershuttle.org:19900
AGENT_ID=test

build-container:
	docker build --platform linux/x86_64 -t $(IMAGE_NAME) .; \
	docker push $(IMAGE_NAME)

run-container:
	docker run -it $(IMAGE_NAME) /opt/airavata-agent $(AGENT_SVC_URL) $(AGENT_ID)
