from django_airavata.app_config import AiravataAppConfig


class DataParsersConfig(AiravataAppConfig):
    name = 'django_airavata.apps.dataparsers'
    label = 'django_airavata_dataparsers'
    verbose_name = 'Data Parsers'
    url_app_name = label
    app_order = 20
    url_home = 'django_airavata_dataparsers:home'
    fa_icon_class = 'fa-files-o'
    app_description = """
        Define data parsers for post-processing experimental and ad-hoc
        datasets.
    """
