from urllib.parse import urlencode

from django.urls import reverse


def get_download_url(data_product_uri):
    """Get URL for downloading data product identified by data_product_uri."""
    return (reverse("django_airavata_api:download_file") + "?" +
            urlencode({"data-product-uri": data_product_uri}))
