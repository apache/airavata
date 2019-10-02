import json
import logging
import os
from urllib.parse import urlparse

from django.conf import settings

logger = logging.getLogger(__name__)


def move_tus_upload(upload_url, move_function):
    """
    Move upload identified by upload_url using the provided move_function.

    move_function signature should be (file_path, file_name). It's
    return value will be returned.
    """
    # file UUID is last path component in URL. For example:
    # http://localhost:1080/files/2c44415fdb6259a22f425145b87d0840
    upload_uuid = urlparse(upload_url).path.split("/")[-1]
    upload_bin_path = os.path.join(settings.TUS_DATA_DIR,
                                   f"{upload_uuid}.bin")
    logger.debug(f"upload_bin_path={upload_bin_path}")
    upload_info_path = os.path.join(settings.TUS_DATA_DIR,
                                    f"{upload_uuid}.info")
    with open(upload_info_path) as upload_info_file:
        upload_info = json.load(upload_info_file)
        filename = upload_info['MetaData']['filename']
        result = move_function(upload_bin_path, filename)
    os.remove(upload_info_path)
    return result
