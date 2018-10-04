
from django_airavata.app_config import AiravataAppConfig


class AdminConfig(AiravataAppConfig):
    name = 'django_airavata.apps.admin'
    label = 'django_airavata_admin'
    verbose_name = 'Admin'
    url_app_name = label
    app_order = 100
    url_home = url_app_name + ':home'
    fa_icon_class = 'fa-cog'
    app_description = """
        Configure and share resources with other users.
    """
