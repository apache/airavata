FROM cybershuttle/remote-agent-base:latest

USER root

RUN micromamba install -y -n base -c conda-forge \
    python \
    pip \
    ipykernel \
    numba==0.56.3 \
    gdal \
    fiona   \
    rasterio \
    ipyleaflet \
    scipy \
    bsddb3 \
    qgis \
    scikit-learn \
    && micromamba clean --all -y

RUN micromamba run -n base pip install --no-cache-dir \
    wget \
    jupyter_contrib_nbextensions \
    utm \
    matplotlib \
    matplotlib_scalebar \
    geopandas \
    folium \
    earthpy \
    patool \
    cdsapi \
    geemap \
    gdown \
    geojson \
    pyarrow \
    xarray \
    pooch \
    zarr \
    cython \
    dask \
    fsspec \
    gcsfs \
    intake \
    intake-esm \
    cmaps \
    cartopy \
    regionmask \
    cdo-api-py \
    pynhd \
    progressbar \
    hydrofunctions \
    googledrivedownloader \
    cartopy \
    s3fs \
    pywbt \
    cfgrib
