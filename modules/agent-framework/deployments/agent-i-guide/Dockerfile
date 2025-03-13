FROM cybershuttle/remote-agent-base:latest

USER root

RUN micromamba install -y -c conda-forge \
    pandas \
    geopandas \
    pygeos \
    matplotlib \
    numpy \
    gdal \
    libpysal \
    esda \
    contextily \
    && micromamba clean --all -y
