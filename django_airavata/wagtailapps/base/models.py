from __future__ import unicode_literals

from django.db import models

from modelcluster.fields import ParentalKey
from modelcluster.models import ClusterableModel

from wagtail.admin.edit_handlers import (
    FieldPanel,
    FieldRowPanel,
    InlinePanel,
    MultiFieldPanel,
    PageChooserPanel,
    StreamFieldPanel,
)
from wagtail.core.fields import RichTextField, StreamField
from wagtail.core.models import Collection, Page, Orderable
from wagtail.core.blocks import RawHTMLBlock
from wagtail.images.edit_handlers import ImageChooserPanel
from wagtail.search import index
from wagtail.snippets.models import register_snippet

from .blocks import BaseStreamBlock
from .blocks import CssStreamBlock
from wagtail.snippets.blocks import SnippetChooserBlock


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
        help_text = 'Provide an announcement text',
        default = 'Announcement Text'
    )
    announcement_link = models.CharField(
        max_length=255,
        help_text = 'Give a redirect link for announcement',
        default = 'Announcement Link'
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
class CustomCss(models.Model):
    """
    Custom CSS
    """

    css = StreamField(CssStreamBlock(), verbose_name="CSS block", blank=True, null=True, help_text="Write custom css and give comments as necessary",default="")

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
    image = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Footer image'
    )
    image_link = models.CharField(
        max_length=255,
        help_text = 'Give a redirect link for images',
        null = True,
        blank = True,
    )
    image_width = models.IntegerField(
        help_text = 'Give a custom image width or leave blank',
        null = True,
        blank = True,
    )
    image_height = models.IntegerField(
        help_text = 'Give a custom image height or leave blank',
        null = True,
        blank = True,
    )

    panels = [
        ImageChooserPanel('image'),
        FieldPanel('image_link'),
        FieldPanel('image_width'),
        FieldPanel('image_height'),
    ]

    def __str__(self):
        return "Footer Text"

    class Meta:
        verbose_name_plural = 'Footer Text'


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
        max_length = 255,
        help_text = 'Provide a redirection link for the logo or logo text Eg. (https://www.google.com/)',
        null=True,
        blank=True,
        default = '#',
    )

    logo_width = models.IntegerField(
        help_text = 'Provide a width for the logo',
        null=True,
        blank=True,
        default = '144',
    )

    logo_height = models.IntegerField(
        help_text = 'Provide a height for the logo',
        null=True,
        blank = True,
        default = '43'
    )

    logo_text = models.CharField(
        max_length=255,
        help_text = 'Give a title text as an alternative to logo. Eg.(SEAGRID)',
        null=True,
        blank=True,
    )

    logo_text_color = models.CharField(
        max_length=100,
        help_text = 'Give a color for logo text if you have a logo text Eg.(#FFFFFF)',
        null=True,
        blank=True,
    )

    logo_text_size = models.IntegerField(
        help_text = 'Give a text size as number of pixels Eg.(30)',
        null=True,
        blank=True,
    )


    panels = [
        ImageChooserPanel('logo'),
        FieldPanel('logo_redirect_link'),
        FieldPanel('logo_width'),
        FieldPanel('logo_height'),
        FieldPanel('logo_text'),
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
        help_text = 'Give a Link text',
    )

    header_link = models.CharField(
        max_length=255,
        help_text = 'Provide a redirect Link',
        null=True,
        blank=True,
    )

    header_sub_link_text1 = models.CharField(
        max_length=25,
        help_text = 'Give a Sub Link 1 text',
        null=True,
        blank=True,
    )

    header_sub_link_text2 = models.CharField(
        max_length=25,
        help_text = 'Give a Sub Link 2 text',
        null=True,
        blank=True,
    )

    header_sub_link_text3 = models.CharField(
        max_length=25,
        help_text = 'Give a Sub Link 3 text',
        null=True,
        blank=True,
    )

    header_sub_link_text4 = models.CharField(
        max_length=25,
        help_text = 'Give a Sub Link 4 text',
        null=True,
        blank=True,
    )

    header_sub_link1 = models.CharField(
        max_length=255,
        help_text = 'Provide a redirect Link for sublink 1',
        null=True,
        blank=True,
    )

    header_sub_link2 = models.CharField(
        max_length=255,
        help_text = 'Provide a redirect Link for sublink 2',
        null=True,
        blank=True,
    )

    header_sub_link3 = models.CharField(
        max_length=255,
        help_text = 'Provide a redirect Link for sublink 3',
        null=True,
        blank=True,
    )

    header_sub_link4 = models.CharField(
        max_length=255,
        help_text = 'Provide a redirect Link for sublink 4',
        null=True,
        blank=True,
    )

    body = models.CharField(
        max_length=255,
        help_text = 'Give a title text',
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
        BaseStreamBlock(), verbose_name="Home content block", blank=True, null=True
    )

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
        help_text = 'Feature Title 1'
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
        help_text = 'Feature Title 2'
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
        help_text = 'Feature Title 3'
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
        help_text = 'Feature Title 4'
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

    def __str__(self):
        return self.title

class AboutPage(Page):
    """
    The About Page. You can see if you visit your site and edit the aboutpage
    """
    # Announcements
    about_title = models.CharField(
        max_length=255,
        help_text = 'Write some title for about page'
    )

    person_logo = models.ForeignKey(
        'wagtailimages.Image',
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name='+',
        help_text='Person image'
    )

    person_occupation = models.CharField(
        max_length=255,
        help_text='Give a person occupation',
        null=True,
        blank=True,
    )

    person_name = models.CharField(
        verbose_name='Person Name',
        max_length=255,
        help_text='Give a person name',
        null=True,
        blank=True,
    )

    body = StreamField(
        BaseStreamBlock(), verbose_name="About content block", blank=True, null=True
    )

    contact_mail = models.CharField(
        max_length=255,
        help_text = 'Contact Mail',
        null=True,
        blank=True,
    )

    contact_phone = models.CharField(
        max_length=12,
        help_text = 'Contact Phone',
        null=True,
        blank=True,
    )

    contact_website = models.CharField(
        max_length=255,
        help_text = 'Contact Website',
        null=True,
        blank=True,
    )

    content_panels = Page.content_panels + [
        MultiFieldPanel([
            ImageChooserPanel('person_logo'),
            FieldPanel('about_title', classname="full"),
            FieldPanel('person_occupation', classname="full"),
            FieldPanel('person_name', classname="full"),
            FieldPanel('contact_mail', classname="full"),
            FieldPanel('contact_phone', classname="full"),
            FieldPanel('contact_website', classname="full"),
            ], heading="Person section"),

        MultiFieldPanel([
            StreamFieldPanel('body'),
        ], heading="Body section", classname="collapsible"),
    ]

    def __str__(self):
        return self.about_title


class ContactPage(Page):
    """
    The Contact Page. You can see if you visit your site and edit the contact page.
    """

    body = StreamField(
        BaseStreamBlock(), verbose_name="Contact content block", blank=True, null=True
    )

    contact_mail = RichTextField(
        null=True,
        blank=True,
        help_text='Give Mail with Text Description'
    )

    contact_phone = RichTextField(
        help_text = 'Give Phone Number with Text Description',
        null=True,
        blank=True,
    )

    contact_website = RichTextField(
        help_text = 'Give Website Link with Text Description',
        null=True,
        blank=True,
    )

    content_panels = Page.content_panels + [
        MultiFieldPanel([
            FieldPanel('contact_mail', classname="full"),
            FieldPanel('contact_phone', classname="full"),
            FieldPanel('contact_website', classname="full"),
            ], heading="Contact section"),

        MultiFieldPanel([
            StreamFieldPanel('body'),
        ], heading="Body section", classname="collapsible"),
    ]

    def __str__(self):
        return self.title

class DocumentationPage(Page):
    """
    The Documentation Page. You can see if you visit your site and edit the documentation page.
    """

    body = StreamField(
        BaseStreamBlock(), verbose_name="Documentation content block", blank=True, null=True
    )

    documentation_title = models.CharField(
        max_length=255,
        help_text = 'Documentation Title',
        null=True,
        blank=True,
    )

    top_body = RichTextField(
        help_text = 'Edit Top Body',
        null=True,
        blank=True,
    )

    bottom_body = RichTextField(
        help_text = 'Edit Bottom Body',
        null=True,
        blank=True,
    )

    focus_text = RichTextField(
        help_text = 'Edit Focus Text',
        null=True,
        blank=True,
    )

    content_panels = Page.content_panels + [
        MultiFieldPanel([
            FieldPanel('documentation_title', classname="full"),
            FieldPanel('focus_text', classname="full"),
            FieldPanel('bottom_body', classname="full"),
            FieldPanel('top_body', classname="full"),
            ], heading="Documentation section"),

        MultiFieldPanel([
            StreamFieldPanel('body'),
        ], heading="Body section", classname="collapsible"),
    ]

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
    page = ParentalKey('django_airavata_wagtail_base.BlankPage', on_delete=models.CASCADE, related_name='row')


class BlankPage(Page):
    """
    The Blank Template Page. You can see if you visit your site and edit the blank page. Used to create free form content
    """

    content_panels = Page.content_panels + [
        InlinePanel("row", label="row")
    ]

    def __str__(self):
        return self.title
