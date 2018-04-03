from wagtail.images.blocks import ImageChooserBlock
from wagtail.embeds.blocks import EmbedBlock
from wagtail.snippets.blocks import SnippetChooserBlock
from wagtail.core.blocks import (
    CharBlock, ChoiceBlock, RichTextBlock, StreamBlock, StructBlock, TextBlock, ListBlock, BooleanBlock, StaticBlock, IntegerBlock, RawHTMLBlock
)
from django_airavata.wagtailapps.base import models

class ImageBlock(StructBlock):
    """
    Custom `StructBlock` for utilizing images with associated caption and
    attribution data
    """
    image = ImageChooserBlock(required=True)
    caption = CharBlock(required=False)
    width = CharBlock(required=False)
    height = IntegerBlock(required=False)
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")

    class Meta:
        icon = 'image'
        template = "blocks/image_block.html"


class ParagraphBlock(StructBlock) :
    """
    Custom 'StructBlock' for creating rich text content
    """
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")
    body = RichTextBlock();

    class Meta:
        icon = "fa-paragraph"
        template = "blocks/paragraph_block.html"
        help_text = "Create a free form paragraph"


class CustomEmbedBlock(StructBlock):
    """
    Custom 'StructBlock' that allows you to embed videos
    """
    embed = EmbedBlock()
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")

    class Meta:
        icon = "fa-link"
        template = "blocks/embed_block.html"
        help_text = "Insert an embed URL e.g https://www.youtube.com/embed/SGJFWirQ3ks"


class CssCommentBlock(StructBlock):
    """
    CSS Comment block
    """
    message = TextBlock(required=True, help_text="Write some comment to mark the css")

    class Meta:
        icon = "fa-comment"
        template = "blocks/css_comment.html"
        help_text = "-----Navbar Styles------"

# BootStrap Components

class BootstrapJumbotron(StructBlock):
    """
    Custom 'StructBlock' that allows the user to make a bootstrap jumbotron
    """
    title = TextBlock()
    body = RichTextBlock()
    button_text = TextBlock(required=False)
    button_link = TextBlock(required=False)
    button_color = ChoiceBlock(choices=[
        ('btn-primary','DEFAULT'),
        ('btn-danger', 'RED' ),
        ('btn-secondary', 'GREY'),
        ('btn-success', 'GREEN'),
        ('btn-warning', 'ORANGE')
    ], blank=True, required=False, help_text="select a button color")
    button_size = ChoiceBlock(choices=[
        ('','DEFAULT'),
        ('btn-lg', 'LARGE'),
        ('btn-sm', 'SMALL')
    ], blank=True, required=False, help_text="select a button size")
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")

    class Meta:
        icon = "fa-indent"
        template = "blocks/bootstrap/jumbotron.html"
        help_text = "Create a bootstrap jumbotron"


class BootstrapButton(StructBlock):
    """
    Custom 'StructBlock' that allows the user to make a bootstrap button
    """
    button_text = TextBlock()
    button_link = TextBlock()
    button_color = ChoiceBlock(choices=[
        ('btn-primary','DEFAULT'),
        ('btn-danger', 'RED'),
        ('btn-secondary', 'GREY'),
        ('btn-success', 'GREEN'),
        ('btn-warning', 'ORANGE')
    ], blank=True, required=False, help_text="select a button color")
    button_size = ChoiceBlock(choices=[
        ('','DEFAULT'),
        ('btn-lg', 'LARGE'),
        ('btn-sm', 'SMALL')
    ], blank=True, required=False, help_text="select a button size")
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")

    class Meta:
        icon = "fa-bold"
        template = "blocks/bootstrap/button.html"
        help_text = "Create a bootstrap button"


class BootstrapAlert(StructBlock):
    """
    Custom 'StructBlock' that allows the user to make a bootstrap alert
    """
    alert_text = TextBlock()
    alert_color = ChoiceBlock(choices=[
        ('alert-primary', 'DEFAULT'),
        ('alert-secondary', 'GREY'),
        ('alert-success', 'GREEN'),
        ('alert-danger', 'RED'),
        ('alert-warning', 'ORANGE'),
        ('alert-dark', 'DARK'),
        ('alert-light', 'LIGHT'),
    ], blank=True, required=False, help_text="select a background color")
    is_link = BooleanBlock(required=False)
    alert_link = TextBlock(required=False)
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")

    class Meta:
        icon = "fa-bell"
        template = "blocks/bootstrap/alert.html"
        help_text = "Create a bootstrap alert"

class BootstrapCard(StructBlock):
    """
    Custom 'StructBlock' that allows the user to make a bootstrap card
    """

    card_width = IntegerBlock(help_text="18 works best for card")
    is_card_img = BooleanBlock(required=False)
    card_img = ImageChooserBlock(required=False)
    card_img_width = IntegerBlock(required=False, help_text="provide an image width")
    card_img_height = IntegerBlock(required=False, help_text="provide an image height")
    card_title = TextBlock()
    card_text = RichTextBlock()
    card_bg_color = ChoiceBlock(choices=[
        ('bg-primary', 'DEFAULT'),
        ('bg-secondary', 'GREY'),
        ('bg-success', 'GREEN'),
        ('bg-danger', 'RED'),
        ('bg-warning', 'ORANGE'),
        ('bg-dark', 'DARK'),
        ('bg-light', 'LIGHT'),
    ], blank=True, required=False, help_text="select a background color")
    card_text_color = ChoiceBlock(choices=[
        ('text-primary', 'DEFAULT'),
        ('text-secondary', 'GREY'),
        ('text-success', 'GREEN'),
        ('text-danger', 'RED'),
        ('text-warning', 'ORANGE'),
        ('text-dark', 'DARK'),
        ('text-light', 'LIGHT'),
    ], blank=True, required=False, help_text="select a text color")
    btn_text = TextBlock(required = False)
    btn_color = ChoiceBlock(choices=[
        ('btn-primary','DEFAULT'),
        ('btn-danger', 'RED'),
        ('btn-secondary', 'GREY'),
        ('btn-success', 'GREEN'),
        ('btn-warning', 'ORANGE')
    ], blank=True, required=False, help_text="select a button color")
    btn_link = TextBlock(required=False)
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")

    class Meta:
        icon = "fa-id-card"
        template = "blocks/bootstrap/card.html"
        help_text = "Create a bootstrap card"


class BootstrapCarousel(StructBlock):
    """
    Custom 'StructBlock' that allows the user to make a bootstrap carousel
    """
    c_image1 = ImageChooserBlock(required=True)
    c_image1_title = TextBlock(required=False, blank=True, help_text="Give a title for image 1")
    c_image1_body = TextBlock(required=False, blank=True, help_text="Give a body for image 1")
    c_image2 = ImageChooserBlock(required=False)
    c_image2_title = TextBlock(required=False, blank=True, help_text="Give a title for image 2")
    c_image2_body = TextBlock(required=False, blank=True, help_text="Give a body for image 2")
    c_image3 = ImageChooserBlock(required=False)
    c_image3_title = TextBlock(required=False, blank=True, help_text="Give a title for image 3")
    c_image3_body = TextBlock(required=False, blank=True, help_text="Give a body for image 3")
    c_image4 = ImageChooserBlock(required=False)
    c_image4_title = TextBlock(required=False, blank=True, help_text="Give a title for image 4")
    c_image4_body = TextBlock(required=False, blank=True, help_text="Give a body for image 4")
    c_image5 = ImageChooserBlock(required=False)
    c_image5_title = TextBlock(required=False, blank=True, help_text="Give a title for image 5")
    c_image5_body = TextBlock(required=False, blank=True, help_text="Give a body for image 5")
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")

    class Meta:
        icon = "fa-film"
        template = "blocks/bootstrap/carousel.html"
        help_text = "Create a bootstrap carousel. Fill the images in order to get optimized display."


class BootstrapWell(StructBlock):
    """
    Custom 'StructBlock' that allows user to make a bootstrap well. (optimized for bootstrap 4 using card)
    """
    message = RichTextBlock(help_text="Enter some message inside well")
    well_bg_color = ChoiceBlock(choices=[
        ('bg-primary', 'DEFAULT'),
        ('bg-secondary', 'GREY'),
        ('bg-success', 'GREEN'),
        ('bg-danger', 'RED'),
        ('bg-warning', 'ORANGE'),
        ('bg-dark', 'DARK'),
        ('bg-light', 'LIGHT'),
    ], blank=True, required=False, help_text="select a background color")
    custom_class = TextBlock(required=False,blank=True, help_text="control this element by giving unique class names separated by space and styling the class in css")

    class Meta:
        icon = "fa-window-minimize"
        template = "blocks/bootstrap/well.html"

# StreamBlocks
class BaseStreamBlock(StreamBlock):
    """
    Define the custom blocks that `StreamField` will utilize
    """
    paragraph_block = ParagraphBlock()
    image_block = ImageBlock()
    embed_block = CustomEmbedBlock()
    bootstrap_jumbotron = BootstrapJumbotron()
    bootstrap_alert = BootstrapAlert()
    bootstrap_button = BootstrapButton()
    bootstrap_card = BootstrapCard()
    bootstrap_carousel = BootstrapCarousel()
    bootstrap_well = BootstrapWell()


class CssStreamBlock(StreamBlock):
    """
    Define the custom blocks for css that 'StreamField' will utilize
    """
    css_block = RawHTMLBlock(required=True, help_text="Write Css Here")
    css_comment = CssCommentBlock()
