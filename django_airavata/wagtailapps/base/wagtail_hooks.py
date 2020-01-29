import logging

import wagtail.admin.rich_text.editors.draftail.features as draftail_features
from django.shortcuts import redirect
from wagtail.admin.rich_text.converters.html_to_contentstate import (
    InlineStyleElementHandler
)
from wagtail.core import hooks

logger = logging.getLogger(__name__)


@hooks.register('register_rich_text_features')
def register_custom_style_feature(features):

    feature_name = 'purple'
    type_ = feature_name.upper()
    tag = 'span'
    detection = '{tag}[class="{feature_name}"]'.format(
        tag=tag, feature_name=feature_name)

    control = {
        'type': type_,
        'description': 'Purple Color',
        # This should be an svg which will occupy a 1024x1024 viewbox
        'icon': ['M100 100 H 900 V 900 H 100 Z'],
        'label': 'purple',
        # .purple is the class which is defined in draft-colors.css . It is necessary to get this style working.
        'style': {'color': 'purple'},
    }

    features.register_editor_plugin(
        'draftail', feature_name, draftail_features.InlineStyleFeature(control)
    )

    db_conversion = {
        'from_database_format': {detection: InlineStyleElementHandler(type_)},
        'to_database_format': {'style_map': {type_: {'element': tag, 'props': {'class': feature_name}}}},
    }

    features.register_converter_rule(
        'contentstate', feature_name, db_conversion)

    features.default_features.append(feature_name)


DIRECT_SERVE_FILE_EXTENSIONS = ["pdf"]


@hooks.register('before_serve_document')
def direct_serve_document(document, request):
    try:
        file_extension = document.file.name.split(".")[-1]
        if (file_extension in DIRECT_SERVE_FILE_EXTENSIONS and
                'download' not in request.GET):
            return redirect(document.file.url)
    except Exception as e:
        logger.warning("direct_serve_document error: ", exc_info=e)
        return None
