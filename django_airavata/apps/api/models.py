from django.db import models


# Create your models here.
class WorkspacePreferences(models.Model):
    username = models.CharField(max_length=64, primary_key=True)
    most_recent_project_id = models.CharField(max_length=64)

    @classmethod
    def create(self, username):
        return WorkspacePreferences(username=username)
