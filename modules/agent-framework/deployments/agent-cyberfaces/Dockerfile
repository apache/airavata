FROM cybershuttle/remote-agent-base:latest

USER root

RUN micromamba install -y -n base -c conda-forge \
    gdal \
    pandas \
    geopandas \
    pygeos \
    matplotlib \
    numpy \
    libpysal \
    esda \
    contextily \
    wget \
    jupyter_contrib_nbextensions \
    utm \
    matplotlib-scalebar \
    earthpy \
    patool \
    cdsapi \
    gdown \
    geojson \
    pyarrow \
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
    cdo \
    progressbar2 \
    googledrivedownloader \
    s3fs \
    pywbt \
    cfgrib \
    tensorflow \
    seaborn \
    earthengine-api \
    folium \
    scikit-learn \
    netCDF4 \
    xarray \
    statsmodels \
    pmdarima \
    pygridmet \
    hydrofunctions \
    pynhd \
    missingno \
    geemap \
    numba \
    scipy \
    rasterio \
    ipyleaflet \
    bsddb3 \
    && micromamba clean --all -y

RUN micromamba run -n base pip install --no-cache-dir --force-reinstall \
    cdo-api-py \
    "typing_extensions>=4.12.0" \
    "numpy<2.0.0"
