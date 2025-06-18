from django.conf import settings
from django.core.files.uploadhandler import (
    StopUpload,
    TemporaryFileUploadHandler
)


class MaxFileSizeTemporaryFileUploadHandler(TemporaryFileUploadHandler):

    def handle_raw_input(self,
                         input_data,
                         META,
                         content_length,
                         boundary,
                         encoding=None):
        """
        Use the content_length to enforce max size limit.
        """
        # Check the content-length header to see if we should
        # If the post is too large, we cannot use the Memory handler.
        if content_length > settings.FILE_UPLOAD_MAX_FILE_SIZE:
            raise StopUpload
