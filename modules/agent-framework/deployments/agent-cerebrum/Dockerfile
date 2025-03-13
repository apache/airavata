FROM cybershuttle/remote-agent-base:latest

USER root

RUN micromamba install -y -n base -c conda-forge \
    ipywidgets \
    numba \
    numpy=1.23.5 \
    matplotlib \
    openpyxl \
    pandas \
    pyqtgraph \
    pyyaml \
    requests \
    scipy \
    sqlalchemy \
    tqdm \
    nest-simulator \
    ipytree \
    python-jsonpath \
    pydantic=2.7 \
    anndata \
    parse \
    && micromamba clean --all -y

RUN micromamba run -n base pip install --no-cache-dir \
    allensdk \
    bmtk \
    pytree \
    git+https://github.com/alleninstitute/abc_atlas_access \
    git+https://github.com/alleninstitute/neuroanalysis \
    git+https://github.com/alleninstitute/aisynphys  \
    git+https://github.com/lahirujayathilake/mousev1

RUN micromamba run -n base pip install --no-cache-dir --no-deps \
    git+https://github.com/apache/airavata-cerebrum
