from django_airavata.app_config import AiravataAppConfig


class WorkspaceConfig(AiravataAppConfig):
    name = 'django_airavata.apps.workspace'
    label = 'django_airavata_workspace'
    verbose_name = 'Workspace'
    url_app_name = label
    app_order = 0
    url_home = 'django_airavata_workspace:dashboard'
    fa_icon_class = 'fa-flask'
    app_description = """
        Launch applications and manage your experiments and projects.
    """

    def ready(self):
        import django_airavata.apps.workspace.signals  # noqa
