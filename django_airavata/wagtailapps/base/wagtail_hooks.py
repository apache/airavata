import wagtail.admin.rich_text.editors.draftail.features as draftail_features
from wagtail.admin.rich_text.converters.html_to_contentstate import InlineStyleElementHandler
from wagtail.core import hooks

@hooks.register('register_rich_text_features')
def register_custom_style_feature(features):

    feature_name = 'purple' # .mycustomstyle will have to be defined in the CSS in order to get frontend styles working
    type_ = feature_name.upper()
    tag = 'span'
    detection = f'{tag}[class="{feature_name}"]'

    control = {
        'type': type_,
        'description': 'Purple Color',
        'label': 'purple',
        'style': {'color': 'purple'}, # let's say .mycustomstyle changes text color to purple
    }

    features.register_editor_plugin(
        'draftail', feature_name, draftail_features.InlineStyleFeature(control)
    )

    db_conversion = {
        'from_database_format': {detection: InlineStyleElementHandler(type_)},
        'to_database_format': {'style_map': {type_: {'element': tag, 'props': {'class': feature_name}}}},
        # The following line works too but I suppose it's not the proper way to do it
        #'to_database_format': {'style_map': {type_: f'{tag} class="{feature_name}"'}},
    }

    features.register_converter_rule('contentstate', feature_name, db_conversion)

    features.default_features.append(feature_name)
