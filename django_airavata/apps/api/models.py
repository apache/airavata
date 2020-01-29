from django.db import models


# Create your models here.
class WorkspacePreferences(models.Model):
    username = models.CharField(max_length=64, primary_key=True)
    most_recent_project_id = models.CharField(max_length=255, null=True)
    most_recent_group_resource_profile_id = models.CharField(max_length=255,
                                                             null=True)
    most_recent_compute_resource_id = models.CharField(max_length=255,
                                                       null=True)

    @classmethod
    def create(cls, username):
        return WorkspacePreferences(username=username)


class ApplicationPreferences(models.Model):
    username = models.CharField(max_length=64)
    application_id = models.CharField(max_length=64)
    favorite = models.BooleanField(default=False)
    workspace_preferences = models.ForeignKey(WorkspacePreferences,
                                              on_delete=models.CASCADE)

    class Meta:
        unique_together = (('username', 'application_id'),)


class User_Notifications(models.Model):
    class Meta:
        unique_together = (('username', 'notification_id'),)

    username = models.CharField(max_length=64)
    notification_id = models.CharField(max_length=255)
    is_read = models.BooleanField(default=False)


class User_Files(models.Model):
    username = models.CharField(max_length=64)
    file_path = models.TextField()
    file_dpu = models.CharField(max_length=255, primary_key=True)

    class Meta:
        indexes = [
            # FIXME: ideally we would include file_path in the index to make
            # lookups faster, but Django/MariaDB don't support key length on a
            # TEXT column which is required to create an index
            models.Index(fields=['username'], name='username_idx')
        ]
