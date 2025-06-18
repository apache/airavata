from wagtail.images.formats import Format, register_image_format

register_image_format(Format('brand-logo', 'Brand Logo',
                             'richtext-image seagrid-logo', 'max-140x43'))
register_image_format(
    Format(
        'footer-image',
        'Footer Image',
        'richtext-image original img-responsive',
        'max-250x250'))
