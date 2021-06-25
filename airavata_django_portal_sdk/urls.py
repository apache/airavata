import warnings
from urllib.parse import urlencode

from django.urls import path, reverse

from . import views


def get_download_url(data_product_uri):
    """(Deprecated) Get URL for downloading data product identified by data_product_uri."""
    warnings.warn("Use user_storage.get_download_url instead.", DeprecationWarning)
    return (reverse("django_airavata_api:download_file") + "?" +
            urlencode({"data-product-uri": data_product_uri}))


app_name = 'airavata_django_portal_sdk'
urlpatterns = [
    path('download-file', views.download_file, name='download_file'),
    path('download', views.download, name='download'),
    path('download-dir', views.download_dir, name='download_dir'),
    path('download-experiment-dir/<experiment_id>', views.download_experiment_dir, name='download_experiment_dir'),
]
