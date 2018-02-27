from django_airavata.app_config import AiravataAppConfig


class GroupsConfig(AiravataAppConfig):
    name = 'django_airavata.apps.groups'
    label = 'django_airavata_groups'
    verbose_name = 'Groups'
    url_app_name = label
    app_order = 10
    url_home = url_app_name + ':manage'
    fa_icon_class = 'fa-users'
