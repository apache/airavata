from django.db import models


class UserFiles(models.Model):
    """Base model that should be implemented in Airavata Django Portal."""
    username = models.CharField(max_length=64)
    file_path = models.TextField()
    file_dpu = models.CharField(max_length=255, primary_key=True)

    class Meta:
        indexes = [
            # FIXME: ideally we would include file_path in the index to make
            # lookups faster, but Django/MariaDB don't support key length on a
            # TEXT column which is required to create an index
            models.Index(fields=['username'], name='userfiles_username_idx')
        ]
