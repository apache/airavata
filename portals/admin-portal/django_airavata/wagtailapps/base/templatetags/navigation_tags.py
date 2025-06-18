from django import template
from django.conf import settings
from wagtail.core.models import Page, Site

from django_airavata.wagtailapps.base.models import (
    Announcements,
    CustomCss,
    CustomHeaderLinks,
    ExtraWebResources,
    FooterText,
    GatewayIcon,
    GatewayTitle,
    Navbar,
    NavExtra
)

register = template.Library()
# https://docs.djangoproject.com/en/1.9/howto/custom-template-tags/


@register.simple_tag(takes_context=True)
def get_site_root(context):
    # This returns a core.Page. The main menu needs to have the site.root_page
    # defined else will return an object attribute error ('str' object has no
    # attribute 'get_children')
    return Site.find_for_request(context['request']).root_page


def has_menu_children(page):
    # This is used by the top_menu property
    # get_children is a Treebeard API thing
    # https://tabo.pe/projects/django-treebeard/docs/4.0.1/api.html
    return page.get_children().live().in_menu().exists()


def has_children(page):
    # Generically allow index pages to list their children
    return page.get_children().live().exists()


def is_active(page, current_page):
    # To give us active state on main navigation
    return (current_page.url.startswith(page.url) if current_page else False)


# Retrieves the top menu items - the immediate children of the parent page
# The has_menu_children method is necessary because the Foundation menu requires
# a dropdown class to be applied to a parent
@register.inclusion_tag('tags/top_menu.html', takes_context=True)
def top_menu(context, parent, calling_page=None):
    menuitems = parent.get_children().live().in_menu()
    for menuitem in menuitems:
        menuitem.show_dropdown = has_menu_children(menuitem)
        # We don't directly check if calling_page is None since the template
        # engine can pass an empty string to calling_page
        # if the variable passed as calling_page does not exist.
        menuitem.active = (calling_page.url.startswith(menuitem.url)
                           if calling_page else False)
    return {
        'calling_page': calling_page,
        'menuitems': menuitems,
        # required by the pageurl tag that we want to use within this template
        'request': context['request'],
    }


# Retrieves the children of the top menu items for the drop downs
@register.inclusion_tag('tags/top_menu_children.html', takes_context=True)
def top_menu_children(context, parent, calling_page=None):
    menuitems_children = parent.get_children()
    menuitems_children = menuitems_children.live().in_menu()
    for menuitem in menuitems_children:
        menuitem.has_dropdown = has_menu_children(menuitem)
        # We don't directly check if calling_page is None since the template
        # engine can pass an empty string to calling_page
        # if the variable passed as calling_page does not exist.
        menuitem.active = (calling_page.url.startswith(menuitem.url)
                           if calling_page else False)
        menuitem.children = menuitem.get_children().live().in_menu()
    return {
        'parent': parent,
        'menuitems_children': menuitems_children,
        # required by the pageurl tag that we want to use within this template
        'request': context['request'],
    }


@register.inclusion_tag('tags/breadcrumbs.html', takes_context=True)
def breadcrumbs(context):
    self = context.get('self')
    if self is None or self.depth <= 2:
        # When on the home page, displaying breadcrumbs is irrelevant.
        ancestors = ()
    else:
        ancestors = Page.objects.ancestor_of(
            self, inclusive=True).filter(depth__gt=1)
    return {
        'ancestors': ancestors,
        'request': context['request'],
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/announcement_list.html', takes_context=True)
def get_announcements(context):
    announcementObjects = None
    if Announcements.objects.first() is not None:
        announcementObjects = Announcements.objects.all()

    return {
        'announcements': announcementObjects,
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/footer_text.html', takes_context=True)
def get_footer_text(context):
    footer_text = None
    if FooterText.objects.first() is not None:
        footer_text = FooterText.objects.first()

    return {
        'footer_text': footer_text,
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/navbar.html', takes_context=True)
def get_navbar(context):
    navbar = None
    if Navbar.objects.first() is not None:
        navbar = Navbar.objects.first()

    return {
        'navbar': navbar,
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/custom_header_links.html', takes_context=True)
def get_custom_header_links(context):
    custom_header_links = ""
    if CustomHeaderLinks.objects.first() is not None:
        custom_header_links = CustomHeaderLinks.objects.all()

    return {
        'custom_header_links': custom_header_links,
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/custom_css.html', takes_context=True)
def get_css(context):
    custom_css = ""
    if CustomCss.objects.first() is not None:
        custom_css = CustomCss.objects.first()

    return {
        'custom_css': custom_css,
    }


@register.inclusion_tag(
    'django_airavata_wagtail_base/includes/nav_extra.html', takes_context=True)
def get_nav_extra(context):
    nav_extra = ""
    if NavExtra.objects.first() is not None:
        nav_extra = NavExtra.objects.first()

    return {
        'navextra': nav_extra,
        'request': context['request'],
    }


@register.inclusion_tag(
    'django_airavata_wagtail_base/includes/main_menu_navs.html', takes_context=True)
def main_menu_navs(context):
    """NavExtra nav items that are 'include_in_main_menu' == yes"""
    nav_items = []
    if NavExtra.objects.first() is not None:
        nav_extra = NavExtra.objects.first()
        # only return the nav_items that have 'include_in_main_menu' == yes
        if nav_extra.nav and len(nav_extra.nav) > 0:
            nav = nav_extra.nav[0]
            nav_items = nav.value['nav_items']
            nav_items = filter(lambda n: n.value['include_in_main_menu'] == 'yes', nav_items)

    return {
        'nav_items': nav_items,
        'request': context['request'],
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/gateway_icon.html', takes_context=True)
def gateway_icon(context):
    gateway_icon = GatewayIcon.objects.first()

    return {
        'gateway_icon': gateway_icon
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/gateway_title.html', takes_context=True)
def gateway_title(context):
    gateway_title = GatewayTitle.objects.first()

    return {
        'gateway_title': gateway_title,
        'default_title': settings.PORTAL_TITLE,
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/favicon.html', takes_context=True)
def favicon(context):
    gateway_icon = GatewayIcon.objects.first()
    return {
        'gateway_icon': gateway_icon
    }


@register.inclusion_tag('django_airavata_wagtail_base/includes/extra_web_resources.html',
                        takes_context=True)
def extra_web_resources(context):
    extra_web_resources = ExtraWebResources.objects.first()
    return {
        'extra_web_resources': extra_web_resources,
    }
