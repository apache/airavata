from django.conf import settings
from django.core.management import call_command
from django.test import TestCase, override_settings
from wagtail.core.models import Page, Site


@override_settings(ALLOWED_HOSTS=['example.com'],
                   PORTAL_TITLE="My Portal Title")
class SetWagtailSiteTestCase(TestCase):
    fixtures = ['tests/default.json']

    def test_with_no_sites(self):
        """No Site records, should create default one."""
        Site.objects.all().delete()
        call_command('set_wagtail_site')
        site = Site.objects.get(hostname="example.com")
        self.assertEqual(site.site_name, "My Portal Title")
        self.assertEqual(site.root_page.pk, 3)
        self.assertTrue(site.is_default_site)

    def test_with_localhost_default_site(self):
        """localhost default record exists, should delete and create one."""
        self.assertTrue(Site.objects.filter(hostname="localhost",
                                            is_default_site=True).exists())
        call_command('set_wagtail_site')
        # old default should have been deleted
        self.assertFalse(Site.objects.filter(hostname="localhost",
                                             is_default_site=True).exists())
        site = Site.objects.get(hostname="example.com")
        self.assertEqual(site.site_name, "My Portal Title")
        self.assertEqual(site.root_page.pk, 3)
        self.assertTrue(site.is_default_site)

    def test_with_example_com_default_site(self):
        """Record already exists, shouldn't create one."""
        Site.objects.all().delete()
        site_root = Page.objects.get(pk=3)
        Site.objects.create(
            hostname="example.com",
            is_default_site=True,
            site_name=settings.PORTAL_TITLE,
            root_page=site_root
        )
        call_command('set_wagtail_site')
        # command shouldn't have created a duplicate
        self.assertEqual(1, Site.objects.filter(hostname="example.com",
                                                is_default_site=True).count())

    def test_with_no_airavata_root_page(self):
        """Won't be able to create Site, but should leave existing alone."""
        Page.objects.get(pk=3).delete()
        self.assertTrue(Site.objects.filter(hostname="localhost",
                                            is_default_site=True).exists())
        try:
            call_command('set_wagtail_site')
            self.fail("Should have failed to find site root page")
        except Exception:
            pass
        self.assertTrue(Site.objects.filter(hostname="localhost",
                                            is_default_site=True).exists(),
                        "localhost Site object no longer exists!")
