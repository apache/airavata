FROM python:3.8.13-slim

USER root

RUN pip install pandas
RUN pip install geopandas
RUN pip install pygeos
RUN pip install matplotlib
RUN pip install "numpy<2"
RUN apt update
RUN apt install -y gdal-bin
RUN pip install -U libpysal
RUN pip install -U esda
RUN pip3 install contextily

RUN pip install flask jupyter jupyter-client wget jupyter_contrib_nbextensions \
    utm matplotlib_scalebar earthpy patool cdsapi gdown geojson pyarrow pooch zarr cython dask fsspec gcsfs \
    intake intake-esm cmaps cartopy regionmask cdo-api-py progressbar googledrivedownloader cartopy s3fs pywbt cfgrib \
    tensorflow seaborn earthengine-api folium scikit-learn netCDF4 xarray statsmodels pmdarima \
    pygridmet hydrofunctions pynhd missingno geemap numba==0.56.3 scipy \
    rasterio ipyleaflet bsddb3 scikit-learn
RUN mkdir -p /opt/jupyter
RUN python -m venv /opt/jupyter/venv
ADD airavata-agent-linux /opt/airavata-agent
ADD kernel.py /opt/jupyter/kernel.py