IMAGE_NAME=cybershuttle/remote-agent-cyberfaces
AGENT_SVC_URL=api.gateway.cybershuttle.org:19900
AGENT_ID=test
SSH_IDENTIFIER=x-waterhub@anvil.rcac.purdue.edu
SIF_NAME=agent-framework/container/remote-agent-cyberfaces.sif

build-container:
	docker build --platform linux/x86_64 -t $(IMAGE_NAME) . && \
	docker push $(IMAGE_NAME)

run-container:
	docker run -it $(IMAGE_NAME) /opt/airavata-agent $(AGENT_SVC_URL) $(AGENT_ID)

deploy-container:
	ssh $(SSH_IDENTIFIER) srun -p shared --mem 4G -t 30 singularity pull \
	--disable-cache \
	--force \
	$(SIF_NAME) \
	docker://$(IMAGE_NAME)
