from django.conf import settings
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


class NotificationExtension(models.Model):
    class Meta:
        unique_together = (('notification_id', ),)

    notification_id = models.CharField(max_length=255)
    showInDashboard = models.BooleanField(default=False)


class User_Notifications(models.Model):
    class Meta:
        unique_together = (('username', 'notification_id'),)

    username = models.CharField(max_length=64)
    notification_id = models.CharField(max_length=255)
    is_read = models.BooleanField(default=False)


class ApplicationTemplate(models.Model):
    application_module_id = models.CharField(max_length=255, unique=True)
    template_path = models.TextField()
    created_by = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="+")
    updated_by = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="+")
    created = models.DateTimeField(auto_now_add=True)
    updated = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.application_module_id}: {self.template_path}"


class ApplicationTemplateContextProcessor(models.Model):
    application_template = models.ForeignKey(ApplicationTemplate, on_delete=models.CASCADE, related_name="context_processors")
    # Use django.util.module_loading.import_string to import
    # https://docs.djangoproject.com/en/3.2/ref/utils/#module-django.utils.module_loading
    callable_path = models.CharField(max_length=255)

    class Meta:
        unique_together = (('application_template', 'callable_path'),)

    def __str__(self):
        return self.callable_path


class ApplicationSettings(models.Model):
    application_module_id = models.CharField(max_length=255, unique=True)
    show_queue_settings = models.BooleanField(default=True)
    queue_settings_calculator_id = models.CharField(max_length=255, null=True)
