from django.db import models

class UserFiles(models.Model):
    """Base model that should be implemented in Airavata Django Portal."""
    username = models.CharField(max_length=64)
    file_path = models.TextField()
    file_dpu = models.CharField(max_length=255, primary_key=True)

    class Meta:
        abstract = True
