from __future__ import unicode_literals

from django.db import models
from modelcluster.fields import ParentalKey
from wagtail.admin.edit_handlers import (FieldPanel, InlinePanel,
                                         MultiFieldPanel, ObjectList,
                                         PageChooserPanel, StreamFieldPanel,
                                         TabbedInterface)
from wagtail.core.fields import RichTextField, StreamField
from wagtail.core.models import Orderable, Page
from wagtail.images.edit_handlers import ImageChooserPanel
from wagtail.snippets.models import register_snippet

from .blocks import BaseStreamBlock, CssStreamBlock


@register_snippet
class Announcements(models.Model):
    """
    This provides editable text for the site announcements. Again it uses the decorator
    `register_snippet` to allow it to be accessible via the admin. It is made
    accessible on the template via a template tag defined in base/templatetags/
    navigation_tags.py
    """
    announcement_text = models.CharField(
        max_length=255,
        help_text='Provide an announcement text',
        default='Announcement Text'
    )
    announcement_link = models.CharField(
        max_length=255,
        help_text='Give a redirect link for announcement',
        default='Announcement Link'
    )

    panels = [
        FieldPanel('announcement_text'),
        FieldPanel('announcement_link'),
    ]

    def __str__(self):
        return "Announcement"

    class Meta:
        verbose_name_plural = 'Announcement'


@register_snippet
class NavExtra(models.Model):
    """
    This provides editable text for the site extra navbar which comes below the main navbar. Again it uses the decorator
    `register_snippet` to allow it to be accessible via the admin. It is made
    accessible on the template via a template tag defined in base/templatetags/
    navigation_tags.py
    """
    nav_logo = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Nav Extra Logo'
    )
    nav_logo_width = models.IntegerField(
        null=True, blank=True, help_text="Navbar Logo width")
    nav_logo_height = models.IntegerField(
        null=True, blank=True, help_text="Navbar Logo height")
    nav_logo_link = models.CharField(
        max_length=255,
        default="#",
        help_text="Give a redirect link for the Logo")
    nav_text1 = models.CharField(
        max_length=25, help_text="Give a text for link 1")
    faicon1 = models.CharField(
        max_length=50,
        help_text="Provide a class name of icon from font awesome website")
    nav_link1 = models.CharField(
        max_length=255, help_text="Provide a link address for link 1")
    nav_text2 = models.CharField(
        max_length=25,
        help_text="Give a text for link 2",
        null=True,
        blank=True)
    faicon2 = models.CharField(
        max_length=50,
        help_text="Provide a class name of icon from font awesome website",
        null=True,
        blank=True)
    nav_link2 = models.CharField(
        max_length=255,
        help_text="Provide a link address for link 2",
        null=True,
        blank=True)
    nav_text3 = models.CharField(
        max_length=25,
        help_text="Give a text for link 3",
        null=True,
        blank=True)
    faicon3 = models.CharField(
        max_length=50,
        help_text="Provide a class name of icon from font awesome website",
        null=True,
        blank=True)
    nav_link3 = models.CharField(
        max_length=255,
        help_text="Provide a link address for link 3",
        null=True,
        blank=True)
    custom_class = models.CharField(
        max_length=255,
        help_text="Provide custom class names separated by space to gain extra control of nav",
        null=True,
        blank=True,
    )

    panels = [
        ImageChooserPanel('nav_logo'),
        FieldPanel('nav_logo_width'),
        FieldPanel('nav_logo_height'),
        FieldPanel('nav_logo_link'),
        FieldPanel('nav_text1'),
        FieldPanel('faicon1'),
        FieldPanel('nav_link1'),
        FieldPanel('nav_text2'),
        FieldPanel('faicon2'),
        FieldPanel('nav_link2'),
        FieldPanel('nav_text3'),
        FieldPanel('faicon3'),
        FieldPanel('nav_link3'),
        FieldPanel('custom_class')
    ]

    def __str__(self):
        return "Nav extra"

    class Meta:
        verbose_name_plural = 'Nav extra'


@register_snippet
class CustomCss(models.Model):
    """
    Custom CSS
    """

    css = StreamField(
        CssStreamBlock(),
        verbose_name="CSS block",
        blank=True,
        null=True,
        help_text="Write custom css and give comments as necessary",
        default="")

    panels = [
        StreamFieldPanel('css'),
    ]

    def __str__(self):
        return "Custom Css"

    class Meta:
        verbose_name_plural = 'Custom CSS'


@register_snippet
class FooterText(models.Model):
    """
    This provides editable text for the site footer. Again it uses the decorator
    `register_snippet` to allow it to be accessible via the admin. It is made
    accessible on the template via a template tag defined in base/templatetags/
    navigation_tags.py
    """
    footer = StreamField(
        BaseStreamBlock(),
        verbose_name="Footer content block",
        blank=True,
        null=True)

    panels = [
        StreamFieldPanel('footer'),
    ]

    def __str__(self):
        return "Footer"

    class Meta:
        verbose_name_plural = 'Footer'


@register_snippet
class Navbar(models.Model):
    """
    This provides editable text for the site header title. Again it uses the decorator
    `register_snippet` to allow it to be accessible via the admin. It is made
    accessible on the template via a template tag defined in base/templatetags/
    navigation_tags.py
    """
    logo = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Brand Logo'
    )

    logo_redirect_link = models.CharField(
        max_length=255,
        help_text='Provide a redirection link for the logo or logo text Eg. (https://www.google.com/)',
        null=True,
        blank=True,
        default='#',
    )

    boolean_choices = (
        ("yes", "Yes"),
        ("no", "No")
    )

    logo_with_text = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want to display the text next to the logo",
        default="no")

    logo_width = models.IntegerField(
        help_text='Provide a width for the logo',
        null=True,
        blank=True,
        default='144',
    )

    logo_height = models.IntegerField(
        help_text='Provide a height for the logo',
        null=True,
        blank=True,
        default='43'
    )

    logo_text = models.CharField(
        max_length=255,
        help_text='Give a title text as an alternative to logo. Eg.(SEAGRID)',
        null=True,
        blank=True,
    )

    logo_text_color = models.CharField(
        max_length=100,
        help_text='Give a color for logo text if you have a logo text Eg.(#FFFFFF)',
        null=True,
        blank=True,
    )

    logo_text_size = models.IntegerField(
        help_text='Give a text size as number of pixels Eg.(30)',
        null=True,
        blank=True,
    )

    panels = [
        ImageChooserPanel('logo'),
        FieldPanel('logo_redirect_link'),
        FieldPanel('logo_width'),
        FieldPanel('logo_height'),
        FieldPanel('logo_text'),
        FieldPanel('logo_with_text'),
        FieldPanel('logo_text_size'),
        FieldPanel('logo_text_color'),
    ]

    def __str__(self):
        return "Navbar"

    class Meta:
        verbose_name_plural = 'Navbar'


@register_snippet
class CustomHeaderLinks(models.Model):
    """
    This provides feasibility for custom links inside header. Otherwise headerlinks are generated dynamically when a new page is created. The sublinks are restricted to 4 per link
    """
    header_link_text = models.CharField(
        max_length=25,
        help_text='Give a Link text',
    )

    header_link = models.CharField(
        max_length=255,
        help_text='Provide a redirect Link',
        null=True,
        blank=True,
    )

    header_sub_link_text1 = models.CharField(
        max_length=25,
        help_text='Give a Sub Link 1 text',
        null=True,
        blank=True,
    )

    header_sub_link_text2 = models.CharField(
        max_length=25,
        help_text='Give a Sub Link 2 text',
        null=True,
        blank=True,
    )

    header_sub_link_text3 = models.CharField(
        max_length=25,
        help_text='Give a Sub Link 3 text',
        null=True,
        blank=True,
    )

    header_sub_link_text4 = models.CharField(
        max_length=25,
        help_text='Give a Sub Link 4 text',
        null=True,
        blank=True,
    )

    header_sub_link1 = models.CharField(
        max_length=255,
        help_text='Provide a redirect Link for sublink 1',
        null=True,
        blank=True,
    )

    header_sub_link2 = models.CharField(
        max_length=255,
        help_text='Provide a redirect Link for sublink 2',
        null=True,
        blank=True,
    )

    header_sub_link3 = models.CharField(
        max_length=255,
        help_text='Provide a redirect Link for sublink 3',
        null=True,
        blank=True,
    )

    header_sub_link4 = models.CharField(
        max_length=255,
        help_text='Provide a redirect Link for sublink 4',
        null=True,
        blank=True,
    )

    body = models.CharField(
        max_length=255,
        help_text='Give a title text',
        null=True,
        blank=True,
    )

    panels = [
        FieldPanel('header_link_text'),
        FieldPanel('header_link'),
        MultiFieldPanel([
            MultiFieldPanel([
                FieldPanel('header_sub_link_text1'),
                FieldPanel('header_sub_link1'),
            ]),
            MultiFieldPanel([
                FieldPanel('header_sub_link_text2'),
                FieldPanel('header_sub_link2'),
            ]),
            MultiFieldPanel([
                FieldPanel('header_sub_link_text3'),
                FieldPanel('header_sub_link3'),
            ]),
            MultiFieldPanel([
                FieldPanel('header_sub_link_text4'),
                FieldPanel('header_sub_link4'),
            ])
        ], heading="Sub Links section", classname="collapsible"),
    ]

    def __str__(self):
        return "Header Custom Links"

    class Meta:
        verbose_name_plural = 'Header Custom Links'


@register_snippet
class GatewayIcon(models.Model):
    """
    Image icon displayed in the header for logged in users.
    """

    icon = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Choose Gateway Icon with dimensions 70x70'
    )
    background_color = models.CharField(
        max_length=10,
        default="#EEEEEE",
        help_text='Background color for icon (e.g. #FFFFFF)',
    )

    panels = [
        ImageChooserPanel('icon'),
        FieldPanel('background_color'),
    ]

    def __str__(self):
        return "Gateway Icon"

    class Meta:
        verbose_name_plural = 'Gateway Icon'


@register_snippet
class GatewayTitle(models.Model):
    """
    Title displayed in the header for logged in users.
    """

    title_text = models.CharField(
        max_length=100,
        help_text='Title to display to logged in users.',
    )

    panels = [
        FieldPanel('title_text'),
    ]

    def __str__(self):
        return "Gateway Title: {}".format(self.title_text)

    class Meta:
        verbose_name_plural = 'Gateway Title'


class HomePage(Page):
    """
    The Home Page. This looks slightly more complicated than it is. You can
    see if you visit your site and edit the homepage that it is split between
    a:
    - Hero area
    - Body area
    - A promotional area
    - Moveable featured site sections
    """

    # Hero section of HomePage
    image = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Homepage image'
    )
    hero_text = models.CharField(
        max_length=255,
        help_text='Write an introduction for the bakery',
        null=True,
        blank=True,
    )
    hero_cta = models.CharField(
        verbose_name='Hero CTA',
        max_length=255,
        help_text='Text to display on Call to Action',
        null=True,
        blank=True,
    )
    hero_cta_link = models.ForeignKey(
        'wagtailcore.Page',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        verbose_name='Hero CTA link',
        help_text='Choose a page to link to for the Call to Action'
    )

    # Body section of the HomePage
    body = StreamField(
        BaseStreamBlock(),
        verbose_name="Home content block",
        blank=True,
        null=True)

    # Promo section of the HomePage
    site_logo = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Site Logo'
    )

    features_text = RichTextField(
        null=True,
        blank=True,
        help_text='Write some feature description'
    )

    feature_logo_1 = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Feature Logo 1'
    )

    feature_1_title = models.CharField(
        max_length=255,
        help_text='Feature Title 1'
    )

    feature_1_text = RichTextField(
        null=True,
        blank=True,
        help_text='Write some feature 1 text description'
    )

    feature_logo_2 = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Feature Logo 2'
    )

    feature_2_title = models.CharField(
        max_length=255,
        help_text='Feature Title 2'
    )

    feature_2_text = RichTextField(
        null=True,
        blank=True,
        help_text='Write some feature 2 text description'
    )

    feature_logo_3 = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Feature Logo 3'
    )

    feature_3_title = models.CharField(
        max_length=255,
        help_text='Feature Title 3'
    )

    feature_3_text = RichTextField(
        null=True,
        blank=True,
        help_text='Write some feature 3 text description'
    )

    feature_logo_4 = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Feature Logo 4'
    )

    feature_4_title = models.CharField(
        max_length=255,
        help_text='Feature Title 4'
    )

    feature_4_text = RichTextField(
        null=True,
        blank=True,
        help_text='Write some feature 4 text description'
    )

    custom_body_message = RichTextField(
        null=True,
        blank=True,
        help_text='Write some custom body message!'
    )

    banner_image = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Choose Banner Image'
    )

    boolean_choices = (
        ("yes", "Yes"),
        ("no", "No")
    )

    show_navbar = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want to display the navbar on home page and no if you don't want to.",
        default=True)

    show_nav_extra = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want the secondary navbar to show on home page or no if you don't want to",
        default=True)

    show_footer = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want the Footer to show on home page or no if you don't want to",
        default="yes")

    content_panels = Page.content_panels + [
        MultiFieldPanel([
            ImageChooserPanel('image'),
            FieldPanel('hero_text', classname="full"),
            MultiFieldPanel([
                FieldPanel('hero_cta'),
                PageChooserPanel('hero_cta_link'),
            ])
        ], heading="Hero section"),
        StreamFieldPanel('body'),
        MultiFieldPanel([
            ImageChooserPanel('site_logo'),
            FieldPanel('features_text'),
            MultiFieldPanel([
                ImageChooserPanel('feature_logo_1'),
                FieldPanel('feature_1_title'),
                FieldPanel('feature_1_text'),
            ]),
            MultiFieldPanel([
                ImageChooserPanel('feature_logo_2'),
                FieldPanel('feature_2_title'),
                FieldPanel('feature_2_text'),
            ]),
            MultiFieldPanel([
                ImageChooserPanel('feature_logo_3'),
                FieldPanel('feature_3_title'),
                FieldPanel('feature_3_text'),
            ]),
            MultiFieldPanel([
                ImageChooserPanel('feature_logo_4'),
                FieldPanel('feature_4_title'),
                FieldPanel('feature_4_text'),
            ])
        ], heading="Feature section", classname="collapsible"),
        FieldPanel('custom_body_message'),
        ImageChooserPanel('banner_image')
    ]

    customization_panels = [
        FieldPanel('show_navbar'),
        FieldPanel('show_nav_extra'),
        FieldPanel('show_footer')
    ]

    edit_handler = TabbedInterface([
        ObjectList(content_panels, heading='Content'),
        ObjectList(customization_panels, heading='Customization'),
        ObjectList(Page.promote_panels, heading='Promote'),
        ObjectList(Page.settings_panels, heading='Settings',
                   classname="settings"),
    ])

    def __str__(self):
        return self.title


class Row(models.Model):
    body = StreamField(
        BaseStreamBlock(), verbose_name="Row Content", blank=True, null=True
    )

    panels = [
        StreamFieldPanel('body'),
    ]

    class Meta:
        abstract = True


class RowBlankPageRelation(Orderable, Row):
    page = ParentalKey('django_airavata_wagtail_base.BlankPage',
                       on_delete=models.CASCADE, related_name='row')


class BlankPage(Page):
    """
    The Blank Template Page. You can see if you visit your site and edit the blank page. Used to create free form content
    """

    boolean_choices = (
        ("yes", "Yes"),
        ("no", "No")
    )

    show_navbar = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want to display the navbar on home page and no if you don't want to.",
        default="yes")

    show_nav_extra = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want the secondary navbar to show on home page or no if you don't want to",
        default="yes")

    show_footer = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want the Footer to show on home page or no if you don't want to",
        default="yes")

    show_announcements = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want the Announcements to show up on home page or no if you don't want to",
        default="yes")

    content_panels = Page.content_panels + [
        InlinePanel("row", label="row")
    ]

    customization_panels = [
        FieldPanel('show_navbar'),
        FieldPanel('show_nav_extra'),
        FieldPanel('show_footer'),
        FieldPanel('show_announcements')
    ]

    edit_handler = TabbedInterface([
        ObjectList(content_panels, heading='Content'),
        ObjectList(customization_panels, heading='Customization'),
        ObjectList(Page.promote_panels, heading='Promote'),
        ObjectList(Page.settings_panels, heading='Settings',
                   classname="settings"),
    ])

    def __str__(self):
        return self.title


class RowCybergatewayHomePageRelation(Orderable, Row):
    page = ParentalKey('django_airavata_wagtail_base.CybergatewayHomePage',
                       on_delete=models.CASCADE, related_name='row')


class CybergatewayHomePage(Page):
    """
    The Cybergateway themed template Page
    """

    # Hero section of HomePage
    site_logo = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Site Logo Image'
    )

    site_link = models.CharField(
        max_length=255,
        default="#",
        help_text='Give a site redirect link',
    )

    site_text = models.CharField(
        max_length=50,
        default="#",
        help_text='Give a Site Name',
    )

    site_header = models.CharField(
        max_length=70,
        default="#",
        help_text='Give a Site Header Name',
    )

    site_link1 = models.CharField(
        max_length=70,
        default="#",
        help_text='Give a Site Nav Link [1]',
    )

    site_link_text1 = models.CharField(
        max_length=70,
        help_text='Give a Site Nav Link Text [1]',
    )

    site_link2 = models.CharField(
        max_length=70,
        default='#',
        help_text='Give a Site Nav Link [2]',
    )

    site_link_text2 = models.CharField(
        max_length=70,
        help_text='Give a Site Nav Link Text [2]',
    )

    site_link3 = models.CharField(
        max_length=70,
        default="#",
        help_text='Give a Site Nav Link [3]',
    )

    site_link_text3 = models.CharField(
        max_length=70,
        help_text='Give a Site Nav Link Text [3]',
    )

    contact = StreamField(
        BaseStreamBlock(),
        verbose_name="Contact Info Block",
        blank=True,
        null=True)

    footer = StreamField(
        BaseStreamBlock(),
        verbose_name="Footer Content Block",
        blank=True,
        null=True)

    boolean_choices = (
        ("yes", "Yes"),
        ("no", "No")
    )

    show_navbar = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want to display the navbar on home page and no if you don't want to.",
        default="yes")

    show_nav_extra = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want the secondary navbar to show on home page or no if you don't want to",
        default="yes")

    show_footer = models.CharField(
        choices=boolean_choices,
        max_length=5,
        help_text="Choose yes if you want the Footer to show on home page or no if you don't want to",
        default="yes")

    content_panels = Page.content_panels + [
        MultiFieldPanel([
            ImageChooserPanel('site_logo'),
            FieldPanel('site_link'),
            FieldPanel('site_text'),
            FieldPanel('site_header'),
            FieldPanel('site_link1'),
            FieldPanel('site_link_text1'),
            FieldPanel('site_link2'),
            FieldPanel('site_link_text2'),
            FieldPanel('site_link3'),
            FieldPanel('site_link_text3'),
        ], heading="Navbar Section"),
        InlinePanel("row", label="row"),
        StreamFieldPanel('contact'),
        StreamFieldPanel('footer'),
    ]

    customization_panels = [
        FieldPanel('show_navbar'),
        FieldPanel('show_nav_extra'),
        FieldPanel('show_footer'),
    ]

    edit_handler = TabbedInterface([
        ObjectList(content_panels, heading='Content'),
        ObjectList(customization_panels, heading='Customization'),
        ObjectList(Page.promote_panels, heading='Promote'),
        ObjectList(Page.settings_panels, heading='Settings',
                   classname="settings"),
    ])

    def __str__(self):
        return self.title
