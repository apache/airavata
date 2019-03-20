
from django_airavata.app_config import AiravataAppConfig


class AdminConfig(AiravataAppConfig):
    name = 'django_airavata.apps.admin'
    label = 'django_airavata_admin'
    verbose_name = 'Admin'
    app_order = 100
    url_home = 'django_airavata_admin:home'
    fa_icon_class = 'fa-cog'
    app_description = """
        Configure and share resources with other users.
    """
