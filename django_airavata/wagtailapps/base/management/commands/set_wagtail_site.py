from django.conf import settings
from django.core.management.base import BaseCommand
from django.db import transaction
from wagtail.core.models import Page, Site

from django_airavata.wagtailapps.base.models import (
    BlankPage,
    CybergatewayHomePage,
    HomePage
)


class Command(BaseCommand):

    def handle(self, **options):
        hostname = settings.ALLOWED_HOSTS[0] if len(
            settings.ALLOWED_HOSTS) > 0 else "localhost"
        if not Site.objects.filter(hostname=hostname,
                                   is_default_site=True).exists():
            with transaction.atomic():
                # Delete any current default site
                Site.objects.filter(is_default_site=True).delete()
                roots = Page.get_root_nodes()
                site_root = self.find_root_airavata_page(roots)
                if site_root is None:
                    raise Exception("Could not find site root page!")
                else:
                    print(f"Setting root page to {site_root.title}")
                Site.objects.create(
                    hostname=hostname,
                    is_default_site=True,
                    site_name=settings.PORTAL_TITLE,
                    root_page=site_root
                )
                print(f"Created Site object for domain {hostname}")
        else:
            print(f"Site object for domain {hostname} already exists")

    def find_root_airavata_page(self, pages):
        for page in pages:
            if (isinstance(page.specific, HomePage) or
                isinstance(page.specific, BlankPage) or
                    isinstance(page.specific, CybergatewayHomePage)):
                return page
            elif not page.is_leaf():
                return self.find_root_airavata_page(page.get_children())
            else:
                return None
