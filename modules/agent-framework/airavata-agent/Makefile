IMAGE_NAME=cybershuttle/remote-agent-base
AGENT_SVC_URL=api.gateway.cybershuttle.org:19900
AGENT_ID=test

build-multiarch:
	GOOS=linux GOARCH=amd64 go build -o airavata-agent-linux-amd64; \
	GOOS=linux GOARCH=arm64 go build -o airavata-agent-linux-arm64; \
	GOOS=darwin GOARCH=amd64 go build -o airavata-agent-osx-amd64; \
	GOOS=darwin GOARCH=arm64 go build -o airavata-agent-osx-arm64

build-container:
	GOOS=linux GOARCH=amd64 go build -o airavata-agent && \
	docker build --platform linux/amd64 -t $(IMAGE_NAME) . && \
	docker push $(IMAGE_NAME)

run-container:
	docker run -it $(IMAGE_NAME) /opt/airavata-agent $(AGENT_SVC_URL) $(AGENT_ID)

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

deploy-bridges2-gcommuni:
	ssh gcommuni@bridges2 \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-delta-svcscigapgwuser:
	ssh svcscigapgwuser@delta \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

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

deploy-pace-ice-ideas-cybershuttle:
	ssh ideas-cybershuttle@pace-ice \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-pace-phoenix-ideas-cybershuttle:
	ssh ideas-cybershuttle@pace-phoenix \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-stampede3-ccguser:
	ssh ccguser@stampede3 \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-vc-airavata-cpu-exouser:
	ssh exouser@vc-airavata-cpu \
	"module load singularity && srun -p cloud -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-vc-airavata-gpu-exouser:
	ssh exouser@vc-airavata-gpu \
	"module load singularity && srun -p cloud -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-vc-gkeyll-exouser:
	ssh exouser@vc-gkeyll \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-vc-gkeyll-gateway-user:
	ssh gateway-user@vc-gkeyll \
	"srun -N1 -n1 -p shared --mem 4G -t 30 \
	singularity pull --disable-cache --force \
	cybershuttle/container/remote-agent-base.sif \
	docker://$(IMAGE_NAME)"

deploy-bootstrap:
	scp agent.sh x-scigap@anvil:~/cybershuttle/agent.sh & \
	scp agent.sh x-waterhub@anvil:~/cybershuttle/agent.sh & \
	scp agent.sh x-gcommunityus@anvil:~/cybershuttle/agent.sh & \
	scp agent.sh gcommuni@bridges2:~/cybershuttle/agent.sh & \
	scp agent.sh svcscigapgwuser@delta:~/cybershuttle/agent.sh & \
	scp agent.sh scigap@expanse:~/cybershuttle/agent.sh & \
	scp agent.sh gridchem@expanse:~/cybershuttle/agent.sh & \
	scp agent.sh ideas-cybershuttle@pace-ice:~/cybershuttle/agent.sh & \
	scp agent.sh ideas-cybershuttle@pace-phoenix:~/cybershuttle/agent.sh & \
	scp agent.sh ccguser@stampede3:~/cybershuttle/agent.sh & \
	scp agent.sh exouser@vc-airavata-cpu:~/cybershuttle/agent.sh & \
	scp agent.sh exouser@vc-airavata-gpu:~/cybershuttle/agent.sh & \
	scp agent.sh exouser@vc-gkeyll:~/cybershuttle/agent.sh & \
	scp agent.sh gateway-user@vc-gkeyll:~/cybershuttle/agent.sh & \
	wait

deploy-anvil: deploy-anvil-scigap deploy-anvil-waterhub deploy-anvil-gcommunityus
deploy-bridges2: deploy-bridges2-gcommuni
deploy-delta: deploy-delta-svcscigapgwuser
deploy-expanse: deploy-expanse-scigap deploy-expanse-gridchem
deploy-pace-ice: deploy-pace-ice-ideas-cybershuttle
deploy-pace-phoenix: deploy-pace-phoenix-ideas-cybershuttle
deploy-stampede3: deploy-stampede3-ccguser deploy-stampede3-exouser
deploy-vc-airavata-cpu: deploy-vc-airavata-cpu-exouser
deploy-vc-airavata-gpu: deploy-vc-airavata-gpu-exouser
deploy-vc-gkeyll: deploy-vc-gkeyll-exouser deploy-vc-gkeyll-gateway-user
