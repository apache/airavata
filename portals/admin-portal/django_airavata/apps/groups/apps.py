from django_airavata.app_config import AiravataAppConfig


class GroupsConfig(AiravataAppConfig):
    name = 'django_airavata.apps.groups'
    label = 'django_airavata_groups'
    verbose_name = 'Groups'
    app_order = 10
    url_home = 'django_airavata_groups:manage'
    fa_icon_class = 'fa-users'
    app_description = """
        Create and manage user groups.
    """
    nav = [
        {
            'label': 'Groups',
            'icon': 'fa fa-users',
            'url': 'django_airavata_groups:manage',
        },
    ]
