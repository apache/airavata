from django_airavata.app_config import AiravataAppConfig


class WorkspaceConfig(AiravataAppConfig):
    name = 'django_airavata.apps.workspace'
    label = 'django_airavata_workspace'
    verbose_name = 'Workspace'
    app_order = 0
    url_home = 'django_airavata_workspace:dashboard'
    fa_icon_class = 'fa-flask'
    app_description = """
        Launch applications and manage your experiments and projects.
    """
    nav = [
        {
            'label': 'Dashboard',
            'icon': 'fa fa-tachometer-alt',
            'url': 'django_airavata_workspace:dashboard',
            'active_prefixes': ['applications', 'dashboard']
        },
        {
            'label': 'Experiments',
            'icon': 'fa fa-flask',
            'url': 'django_airavata_workspace:experiments',
            'active_prefixes': ['experiments']
        },
        {
            'label': 'Projects',
            'icon': 'fa fa-box',
            'url': 'django_airavata_workspace:projects',
            'active_prefixes': ['projects']
        },
        {
            'label': 'Storage',
            'icon': 'fa fa-folder-open',
            'url': 'django_airavata_workspace:storage',
            'active_prefixes': ['storage']
        },
    ]
