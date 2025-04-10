IMAGE_NAME=cybershuttle/remote-agent-base
AGENT_SVC_URL=api.gateway.cybershuttle.org:19900
AGENT_ID=test

build-container:
	GOOS=linux GOARCH=amd64 go build -o airavata-agent && \
	docker build --platform linux/amd64 -t $(IMAGE_NAME) . && \
	docker push $(IMAGE_NAME)

run-container:
	docker run -it $(IMAGE_NAME) /opt/airavata-agent $(AGENT_SVC_URL) $(AGENT_ID)

deploy-anvil: deploy-anvil-scigap deploy-anvil-waterhub deploy-anvil-gcommunityus

deploy-anvil-scigap:
	ssh x-scigap@anvil \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-anvil-waterhub:
	ssh x-waterhub@anvil \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-anvil-gcommunityus:
	ssh x-gcommunityus@anvil \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-expanse: deploy-expanse-scigap deploy-expanse-gridchem

deploy-expanse-scigap:
	ssh scigap@expanse \
	"module load singularitypro && srun -N1 -n1 -A ind123 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-expanse-gridchem:
	ssh gridchem@expanse \
	"module load singularitypro && srun -N1 -n1 -A ind123 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-neurodata25-vc: deploy-neurodata25-vc-1 deploy-neurodata25-vc-2

deploy-neurodata25-vc-1:
	ssh exouser@js2.cpu.airavata.org \
	"module load singularity && srun -p cloud -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-neurodata25-vc-2:
	ssh exouser@js2.gpu.airavata.org \
	"module load singularity && srun -p cloud -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-bootstrap:
	scp agent.sh x-scigap@anvil:~/cybershuttle/agent.sh && \
	scp agent.sh x-waterhub@anvil:~/cybershuttle/agent.sh && \
	scp agent.sh x-gcommunityus@anvil:~/cybershuttle/agent.sh && \
	scp agent.sh scigap@expanse:~/cybershuttle/agent.sh && \
	scp agent.sh exouser@js2.cpu.airavata.org:~/cybershuttle/agent.sh && \
	scp agent.sh exouser@js2.gpu.airavata.org:~/cybershuttle/agent.sh
