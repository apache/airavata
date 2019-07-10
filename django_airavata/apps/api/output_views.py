import json
import logging

from django.conf import settings

from airavata.model.application.io.ttypes import DataType

from . import data_products_helper

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


def get_output_views(request, experiment, application_interface=None):
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
                view_config = {
                    'provider-id': output_view_provider_id,
                    'display-type': output_view_provider.display_type,
                    'name': getattr(output_view_provider, 'name',
                                    output_view_provider_id)
                }
                if getattr(output_view_provider, 'immediate', False):
                    # Immediately call generate_data function
                    data = _generate_data(
                        request, output_view_provider, output, experiment)
                    view_config['data'] = data
                output_views[output.name].append(view_config)
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
    if application_interface is not None:
        app_output_view_providers = _get_application_output_view_providers(
            application_interface, experiment_output.name)
        for view_provider in app_output_view_providers:
            if view_provider not in output_view_providers:
                output_view_providers.append(view_provider)
    return output_view_providers


def _get_application_output_view_providers(application_interface, output_name):
    app_output = [o
                  for o in application_interface.applicationOutputs
                  if o.name == output_name]
    if len(app_output) == 1:
        app_output = app_output[0]
    else:
        return []
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


def _generate_data(request,
                   output_view_provider,
                   experiment_output,
                   experiment):
    # TODO: handle URI_COLLECTION also
    logger.debug("getting data product for {}".format(experiment_output.value))
    output_file = None
    if (experiment_output.value and
        experiment_output.type in (DataType.URI,
                                   DataType.STDOUT,
                                   DataType.STDERR) and
            experiment_output.value.startswith("airavata-dp")):
        data_product = request.airavata_client.getDataProduct(
            request.authz_token, experiment_output.value)
        if data_products_helper.exists(request, data_product):
            output_file = data_products_helper.open(request, data_product)
    data = output_view_provider.generate_data(
        experiment_output, experiment, output_file=output_file)
    return data
