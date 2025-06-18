
from django_airavata.app_config import AiravataAppConfig


class AdminConfig(AiravataAppConfig):
    name = 'django_airavata.apps.admin'
    label = 'django_airavata_admin'
    verbose_name = 'Settings'
    app_order = 100
    url_home = 'django_airavata_admin:home'
    fa_icon_class = 'fa-cog'
    app_description = """
        Configure and share resources with other users.
    """
    nav = [
        {
            'label': 'Application Catalog',
            'icon': 'fa fa-cogs',
            'url': 'django_airavata_admin:app_catalog',
            'active_prefixes': ['applications'],
            'enabled': lambda req: (req.is_gateway_admin or
                                    req.is_read_only_gateway_admin),
        },
        {
            'label': 'Manage Users',
            'icon': 'fa fa-users',
            'url': 'django_airavata_admin:users',
            'active_prefixes': ['users', 'extended-user-profile'],
            'enabled': lambda req: (req.is_gateway_admin or
                                    req.is_read_only_gateway_admin),
        },
        {
            'label': 'Experiment Statistics',
            'icon': 'fa fa-chart-bar',
            'url': 'django_airavata_admin:experiment-statistics',
            'active_prefixes': ['experiment-statistics'],
            'enabled': lambda req: (req.is_gateway_admin or
                                    req.is_read_only_gateway_admin),
        },
        {
            'label': 'Credential Store',
            'icon': 'fa fa-lock',
            'url': 'django_airavata_admin:credential_store',
            'active_prefixes': ['credentials']
        },
        {
            'label': 'Group Resource Profile',
            'icon': 'fa fa-server',
            'url': 'django_airavata_admin:group_resource_profile',
            'active_prefixes': ['group-resource-profiles']
        },
        {
            'label': 'Gateway Resource Profile',
            'icon': 'fa fa-tasks',
            'url': 'django_airavata_admin:gateway_resource_profile',
            'active_prefixes': ['gateway-resource-profile'],
            'enabled': lambda req: (req.is_gateway_admin or
                                    req.is_read_only_gateway_admin)
        },
        {
            'label': 'Manage Notices',
            'icon': 'fa fa-bell',
            'url': 'django_airavata_admin:notices',
            'active_prefixes': ['notices'],
            'enabled': lambda req: (req.is_gateway_admin or
                                    req.is_read_only_gateway_admin)
        },
        {
            'label': 'Developer Console',
            'icon': 'fa fa-code',
            'url': 'django_airavata_admin:developers',
            'active_prefixes': ['developers'],
            'enabled': lambda req: (req.is_gateway_admin or
                                    req.is_read_only_gateway_admin)
        },
    ]
