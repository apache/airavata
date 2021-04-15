import warnings
from urllib.parse import urlencode

from django.urls import path, reverse

from . import views


def get_download_url(data_product_uri):
    """(Deprecated) Get URL for downloading data product identified by data_product_uri."""
    warnings.warn("Use user_storage.get_download_url instead.", DeprecationWarning)
    return (reverse("airavata_django_portal_sdk:download_file") + "?" +
            urlencode({"data-product-uri": data_product_uri}))


app_name = 'airavata_django_portal_sdk'
urlpatterns = [
    path('download', views.download_file, name='download_file'),
]
