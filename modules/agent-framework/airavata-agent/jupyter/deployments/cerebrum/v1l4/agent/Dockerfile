FROM cybershuttle/airavata-agent:latest

USER root

RUN apt-get update && apt-get install -y \
    wget \
    git \
    build-essential \
    openmpi-bin \
    libopenmpi-dev && \
    apt-get clean

RUN wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh -O /tmp/miniconda.sh
RUN bash /tmp/miniconda.sh -b -p /opt/conda && rm /tmp/miniconda.sh
RUN /opt/conda/bin/conda clean -t -i -p -y

ENV PATH=/opt/conda/bin:$PATH

COPY v1l4 /opt/v1l4

RUN conda create -n agent-env python=3.10 && conda clean -a

RUN /bin/bash -c "source activate agent-env && \
    conda install -y -c conda-forge \
            ipywidgets=8.1 \
            jupyter \
            numba=0.60 \
            numpy=1.23 \
            matplotlib=3.7 \
            openpyxl=3.1 \
            pandas=1.5 \
            pyqtgraph \
            pyyaml=6.0 \
            requests=2.31 \
            scipy=1.9 \
            sqlalchemy \
            traitlets=5.1 \
            tqdm=4.66 \
            nest-simulator && \
        conda clean -a"

RUN /bin/bash -c "source activate agent-env && \
    pip install \
    ipytree \
    python-jsonpath \
    pydantic==2.7 \
    anndata \
    allensdk \
    bmtk \
    parse \
    pytree \
    flask \
    git+https://github.com/alleninstitute/abc_atlas_access \
    git+https://github.com/alleninstitute/neuroanalysis \
    git+https://github.com/alleninstitute/aisynphys  \
    git+https://github.com/lahirujayathilake/mousev1 && \
    pip install --no-deps git+https://github.com/apache/airavata-cerebrum.git"

ENV PYTHONPATH="/opt/mousev1"
ENV CONDA_DEFAULT_ENV=agent-env
ENV PATH=/opt/conda/envs/agent-env/bin:$PATH
