from django.db import models

# Create your models here.


class UserDataArchive(models.Model):
    created_date = models.DateTimeField(auto_now_add=True)
    updated_date = models.DateTimeField(auto_now=True)
    archive_name = models.CharField(max_length=255)
    archive_path = models.TextField()
    rolled_back = models.BooleanField(default=False)
    max_modification_time = models.DateTimeField()


class UserDataArchiveEntry(models.Model):
    user_data_archive = models.ForeignKey(UserDataArchive, on_delete=models.CASCADE)
    entry_path = models.TextField()
