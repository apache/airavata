import json
import logging

from django.conf import settings

logger = logging.getLogger(__name__)


class DownloadLinkViewProvider:
    display_type = 'download'
    immediate = True

    def generate_data(self, experiment_output, experiment, output_file=None):
        return {
        }


DEFAULT_VIEW_PROVIDERS = {
    'download': DownloadLinkViewProvider()
}


def get_output_views(experiment):
    output_views = {}
    for output in experiment.experimentOutputs:
        output_views[output.name] = []
        output_view_provider_names = _get_output_view_providers(output)
        for output_view_provider_name in output_view_provider_names:
            output_view_provider = None
            if output_view_provider_name in DEFAULT_VIEW_PROVIDERS:
                output_view_provider = DEFAULT_VIEW_PROVIDERS[
                    output_view_provider_name]
            elif output_view_provider_name in settings.OUTPUT_VIEW_PROVIDERS:
                output_view_provider = settings.OUTPUT_VIEW_PROVIDERS[
                    output_view_provider_name]
            else:
                logger.error("Unable to find output view provider with "
                             "name '{}'".format(output_view_provider_name))
            if output_view_provider is not None:
                if getattr(output_view_provider, 'immediate', False):
                    # Immediately call generate_data function
                    # TODO: also pass a file object if URI (and handle
                    # URI_COLLECTION)
                    data = output_view_provider.generate_data(
                        output, experiment)
                    output_views[output.name].append({
                        'provider-id': output_view_provider_name,
                        'display-type': output_view_provider.display_type,
                        'data': data
                    })
                else:
                    output_views[output.name].append({
                        'provider-id': output_view_provider_name,
                        'display-type': output_view_provider.display_type,
                    })
    return output_views


def _get_output_view_providers(experiment_output):
    output_view_providers = []
    logger.debug("experiment_output={}".format(experiment_output))
    if experiment_output.metaData:
        try:
            output_metadata = json.loads(experiment_output.metaData)
            output_view_providers.extend(
                output_metadata['output-view-providers'])
            logger.debug("output_metadata={}".format(output_metadata))
        except Exception as e:
            logger.exception(
                "Failed to parse metadata for output {}".format(
                    experiment_output.name))
    if 'download' not in output_view_providers:
        output_view_providers.insert(0, 'download')
    # if len(output_view_providers) == 0:
    #     output_view_providers.extend(_get_default_view_providers())
    return output_view_providers
