import json
import logging

from django.conf import settings

logger = logging.getLogger(__name__)


class DefaultViewProvider:
    display_type = 'default'
    immediate = True
    name = "Default"

    def generate_data(self, experiment_output, experiment, output_file=None):
        return {
        }


DEFAULT_VIEW_PROVIDERS = {
    'default': DefaultViewProvider()
}


def get_output_views(experiment, application_interface):
    output_views = {}
    for output in experiment.experimentOutputs:
        output_views[output.name] = []
        output_view_provider_ids = _get_output_view_providers(
            output, application_interface)
        for output_view_provider_id in output_view_provider_ids:
            output_view_provider = None
            if output_view_provider_id in DEFAULT_VIEW_PROVIDERS:
                output_view_provider = DEFAULT_VIEW_PROVIDERS[
                    output_view_provider_id]
            elif output_view_provider_id in settings.OUTPUT_VIEW_PROVIDERS:
                output_view_provider = settings.OUTPUT_VIEW_PROVIDERS[
                    output_view_provider_id]
            else:
                logger.error("Unable to find output view provider with "
                             "name '{}'".format(output_view_provider_id))
            if output_view_provider is not None:
                if getattr(output_view_provider, 'immediate', False):
                    # Immediately call generate_data function
                    # TODO: also pass a file object if URI (and handle
                    # URI_COLLECTION)
                    data = output_view_provider.generate_data(
                        output, experiment)
                    output_views[output.name].append({
                        'provider-id': output_view_provider_id,
                        'display-type': output_view_provider.display_type,
                        'data': data,
                        'name': getattr(output_view_provider, 'name', output_view_provider_id)
                    })
                else:
                    output_views[output.name].append({
                        'provider-id': output_view_provider_id,
                        'display-type': output_view_provider.display_type,
                        'name': getattr(output_view_provider, 'name', output_view_provider_id)
                    })
    return output_views


def _get_output_view_providers(experiment_output, application_interface):
    output_view_providers = []
    logger.debug("experiment_output={}".format(experiment_output))
    if experiment_output.metaData:
        try:
            output_metadata = json.loads(experiment_output.metaData)
            logger.debug("output_metadata={}".format(output_metadata))
            if 'output-view-providers' in output_metadata:
                output_view_providers.extend(
                    output_metadata['output-view-providers'])
        except Exception as e:
            logger.exception(
                "Failed to parse metadata for output {}".format(
                    experiment_output.name))
    if 'default' not in output_view_providers:
        output_view_providers.insert(0, 'default')
    # Add in any output view providers defined on the application interface
    app_output_view_providers = _get_application_output_view_providers(
        application_interface, experiment_output.name)
    for view_provider in app_output_view_providers:
        if view_provider not in output_view_providers:
            output_view_providers.append(view_provider)
    return output_view_providers


def _get_application_output_view_providers(application_interface, output_name):
    app_output = [o for o in application_interface.applicationOutputs if o.name == output_name]
    if len(app_output) == 1:
        app_output = app_output[0]
    if app_output.metaData:
        try:
            output_metadata = json.loads(app_output.metaData)
            if 'output-view-providers' in output_metadata:
                return output_metadata['output-view-providers']
        except Exception as e:
            logger.exception(
                "Failed to parse metadata for output {}".format(
                    app_output.name))
    return []
