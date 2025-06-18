from django.db import models


class UserFiles(models.Model):
    """Base model that should be implemented in Airavata Django Portal."""
    username = models.CharField(max_length=64)
    file_path = models.TextField()
    file_dpu = models.CharField(max_length=255, primary_key=True)
    # resource id is either the (legacy) storage resource id that has an
    # associated storage preference in the Gateway Resource Profile, or a
    # resource id to a resource defined in MFT
    file_resource_id = models.CharField(max_length=255)

    class Meta:
        indexes = [
            # See also migration #0003 which adds an index on file_path
            models.Index(fields=['username'], name='userfiles_username_idx')
        ]
