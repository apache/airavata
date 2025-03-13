build-jl-base:
	docker build --platform linux/amd64 -t cybershuttle/jupyterlab-base -f Dockerfile-jl-base . && \
    docker push cybershuttle/jupyterlab-base

build-jl-iguide:
	docker build --platform linux/amd64 -t cybershuttle/jupyterlab-iguide -f Dockerfile-jl-iguide . && \
	docker push cybershuttle/jupyterlab-iguide

run-jl-base:
	docker run -p 18888:8888 -it cybershuttle/jupyterlab-base

run-jl-iguide:
	docker run -p 18888:8888 -it cybershuttle/jupyterlab-iguide
