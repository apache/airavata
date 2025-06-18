from django_airavata.app_config import AiravataAppConfig


class DataParsersConfig(AiravataAppConfig):
    name = 'django_airavata.apps.dataparsers'
    label = 'django_airavata_dataparsers'
    verbose_name = 'Data Parsers'
    app_order = 20
    url_home = 'django_airavata_dataparsers:home'
    fa_icon_class = 'fa-copy'
    app_description = """
        Define data parsers for post-processing experimental and ad-hoc
        datasets.
    """
    nav = [
        {
            'label': 'Home',
            'icon': 'fa fa-home',
            'url': 'django_airavata_dataparsers:home',
        },
    ]

    def enabled(self, request):
        return getattr(request, 'is_gateway_admin', False)
