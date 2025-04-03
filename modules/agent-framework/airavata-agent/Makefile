IMAGE_NAME=cybershuttle/remote-agent-base
AGENT_SVC_URL=api.gateway.cybershuttle.org:19900
AGENT_ID=test

build-container:
	GOOS=linux GOARCH=amd64 go build -o airavata-agent && \
	docker build --platform linux/amd64 -t $(IMAGE_NAME) . && \
	docker push $(IMAGE_NAME)

run-container:
	docker run -it $(IMAGE_NAME) /opt/airavata-agent $(AGENT_SVC_URL) $(AGENT_ID)

deploy-anvil-scigap:
	ssh x-scigap@anvil.rcac.purdue.edu srun -p shared --mem 4G -t 30 singularity pull \
	--disable-cache \
	--force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)

deploy-anvil-waterhub:
	ssh x-waterhub@anvil.rcac.purdue.edu srun -p shared --mem 4G -t 30 singularity pull \
	--disable-cache \
	--force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)

deploy-anvil-gcommunityus:
	ssh x-gcommunityus@anvil.rcac.purdue.edu srun -p shared --mem 4G -t 30 singularity pull \
	--disable-cache \
	--force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)