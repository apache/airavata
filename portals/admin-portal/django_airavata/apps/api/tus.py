import json
import logging
import os
from urllib.parse import urlparse

from django.conf import settings

logger = logging.getLogger(__name__)


def save_tus_upload(upload_url, save_function):
    """
    Save upload identified by upload_url using the provided save_function.

    save_function signature should be (file_path, file_name, file_type) and
    it should save the file somewhere. After calling save_function, the
    original file will be deleted. save_function's return value will be returned.
    """
    # file UUID is last path component in URL. For example:
    # http://localhost:1080/files/2c44415fdb6259a22f425145b87d0840
    upload_uuid = urlparse(upload_url).path.split("/")[-1]
    upload_bin_path = os.path.join(settings.TUS_DATA_DIR,
                                   f"{upload_uuid}.bin")
    logger.debug(f"upload_bin_path={upload_bin_path}")
    upload_info_path = os.path.join(settings.TUS_DATA_DIR,
                                    f"{upload_uuid}.info")
    if os.path.getsize(upload_bin_path) > settings.FILE_UPLOAD_MAX_FILE_SIZE:
        error_message = (f"File size of {upload_bin_path} is greater than "
                         f"the max of {settings.FILE_UPLOAD_MAX_FILE_SIZE} "
                         f"bytes")
        logger.error(error_message)
        os.remove(upload_bin_path)
        os.remove(upload_info_path)
        raise Exception(error_message)
    with open(upload_info_path) as upload_info_file:
        upload_info = json.load(upload_info_file)
        filename = upload_info['MetaData']['filename']
        filetype = upload_info['MetaData']['filetype']
        result = save_function(upload_bin_path, filename, filetype)
    os.remove(upload_bin_path)
    os.remove(upload_info_path)
    return result
