from django.db import models


# Create your models here.
class WorkspacePreferences(models.Model):
    username = models.CharField(max_length=64, primary_key=True)
    most_recent_project_id = models.CharField(max_length=64)

    @classmethod
    def create(self, username):
        return WorkspacePreferences(username=username)

class User_Notifications(models.Model):
    class Meta:
        unique_together = (('username', 'notification_id'),)

    username = models.CharField(max_length=64)
    notification_id = models.CharField(max_length=255)
    is_read = models.BooleanField(default=False)

class User_Files(models.Model):
    username = models.CharField(max_length=64)
    file_path = models.TextField()
    file_dpu = models.CharField(max_length=500, primary_key=True)

    class Meta:
        indexes = [
            models.Index(fields=['username', 'file_path'],
                         name='username_file_path_idx')
        ]
