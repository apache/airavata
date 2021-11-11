import collections
import inspect
import json
import logging
import os
from functools import partial

import nbformat
import papermill as pm
from airavata.model.application.io.ttypes import DataType
from airavata_django_portal_sdk import user_storage
from django.conf import settings
from nbconvert import HTMLExporter

logger = logging.getLogger(__name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))


class DefaultViewProvider:
    display_type = 'default'
    immediate = False
    name = "Default"

    def generate_data(
            self,
            request,
            experiment_output,
            experiment,
            output_file=None,
            **kwargs):
        return {
        }


class ParameterizedNotebookViewProvider:
    display_type = 'notebook'
    name = "Example Parameterized Notebook View"
    # test_output_file = os.path.join(BASE_DIR, "data", "Gaussian.log")

    def generate_data(self,
                      request,
                      experiment_output,
                      experiment,
                      output_file=None,
                      output_dir=None):
        # use papermill to generate the output notebook
        output_file_path = os.path.realpath(output_file.name)
        pm.execute_notebook(
            os.path.join(BASE_DIR, "path", "to", "notebook.ipynb"),
            # TODO: use TemporaryFile instead
            '/tmp/output.ipynb',
            parameters=dict(
                experiment_output={},
                experiment={},
                output_file=output_file_path,
                output_dir=output_dir
            )
        )
        # TODO: convert the output notebook into html format
        output_notebook = nbformat.read('/tmp/output.ipynb', as_version=4)
        html_exporter = HTMLExporter()
        (body, resources) = html_exporter.from_notebook_node(output_notebook)
        # TODO: return the HTML output as the output key
        return {
            'output': body
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
                logger.warning("Unable to find output view provider with "
                               "name '{}'".format(output_view_provider_id))
            if output_view_provider is not None:
                view_config = {
                    'provider-id': output_view_provider_id,
                    'display-type': output_view_provider.display_type,
                    'name': getattr(output_view_provider, 'name',
                                    output_view_provider_id),
                }
                if getattr(output_view_provider, 'immediate', False):
                    # Immediately call generate_data function
                    data = _generate_data(
                        request, output_view_provider, output, experiment)
                    view_config['data'] = data
                else:
                    view_config['data'] = {}
                output_views[output.name].append(view_config)
    return output_views


def _get_output_view_provider(output_view_provider_id):

    if output_view_provider_id in DEFAULT_VIEW_PROVIDERS:
        return DEFAULT_VIEW_PROVIDERS[output_view_provider_id]
    elif output_view_provider_id in settings.OUTPUT_VIEW_PROVIDERS:
        return settings.OUTPUT_VIEW_PROVIDERS[output_view_provider_id]


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
        except Exception:
            logger.exception(
                "Failed to parse metadata for output {}".format(
                    experiment_output.name))
    # Add in any output view providers defined on the application interface
    if application_interface is not None:
        app_output_view_providers = _get_application_output_view_providers(
            application_interface, experiment_output.name)
        for view_provider in app_output_view_providers:
            if view_provider not in output_view_providers:
                output_view_providers.append(view_provider)
    if 'default' not in output_view_providers:
        output_view_providers.insert(0, 'default')
    return output_view_providers


def _get_application_output_view_providers(application_interface, output_name):
    app_output = [o
                  for o in application_interface.applicationOutputs
                  if o.name == output_name]
    if len(app_output) == 1:
        logger.debug("{}: {}".format(output_name, app_output))
        app_output = app_output[0]
    else:
        return []
    if app_output.metaData:
        try:
            output_metadata = json.loads(app_output.metaData)
            if 'output-view-providers' in output_metadata:
                return output_metadata['output-view-providers']
        except Exception:
            logger.exception(
                "Failed to parse metadata for output {}".format(
                    app_output.name))
    return []


def generate_data(request,
                  output_view_provider_id,
                  experiment_output_name,
                  experiment_id,
                  test_mode=False,
                  **kwargs):
    output_view_provider = _get_output_view_provider(output_view_provider_id)
    # TODO if output_view_provider is None, return 404
    experiment = request.airavata_client.getExperiment(
        request.authz_token, experiment_id)
    experiment_output = [o
                         for o in experiment.experimentOutputs
                         if o.name == experiment_output_name]
    # TODO: handle experiment_output not found by name
    experiment_output = experiment_output[0]
    # TODO: add experiment_output_dir
    # convert the extra/interactive arguments to appropriate types
    kwargs = _convert_params_to_type(output_view_provider, kwargs)
    return _generate_data(request,
                          output_view_provider,
                          experiment_output,
                          experiment,
                          test_mode=test_mode,
                          **kwargs)


def _generate_data(request,
                   output_view_provider,
                   experiment_output,
                   experiment,
                   test_mode=False,
                   **kwargs):
    output_files = []
    # test_mode can only be used in DEBUG=True mode
    if test_mode and settings.DEBUG:
        test_output_file = getattr(output_view_provider,
                                   'test_output_file',
                                   None)
        if test_output_file is None:
            raise Exception(f"test_output_file is not set on {output_view_provider}")
        logger.info(f"Using {test_output_file} instead of regular output file")
        output_file = open(test_output_file, 'rb')
        output_files.append(output_file)

    elif (experiment_output.value and
          experiment_output.type in (DataType.URI,
                                     DataType.URI_COLLECTION,
                                     DataType.STDOUT,
                                     DataType.STDERR) and
            experiment_output.value.startswith("airavata-dp")):
        data_product_uris = experiment_output.value.split(",")
        data_products = map(lambda dpid:
                            request.airavata_client.getDataProduct(request.authz_token,
                                                                   dpid),
                            data_product_uris)
        for data_product in data_products:
            if user_storage.exists(request, data_product):
                output_file = user_storage.open_file(request, data_product)
                output_files.append(output_file)

    generate_data_func = output_view_provider.generate_data
    method_sig = inspect.signature(generate_data_func)
    if 'output_files' in method_sig.parameters:
        generate_data_func = partial(generate_data_func, output_files=output_files)
    # TODO: convert experiment and experiment_output to dict/JSON
    data = generate_data_func(request,
                              experiment_output,
                              experiment,
                              output_file=output_files[0] if len(output_files) > 0 else None,
                              **kwargs)
    _process_interactive_params(data)
    return data


def _process_interactive_params(data):
    if 'interactive' in data:
        _convert_options(data)
        for param in data['interactive']:
            if 'type' not in param:
                param['type'] = _infer_interactive_param_type(param)
            # integer type implicitly has a step size of 1
            if param['type'] == "integer" and 'step' not in param:
                param['step'] = 1


def _convert_options(data):
    """Convert interactive options to explicit text/value dicts."""
    for param in data['interactive']:
        if 'options' in param and isinstance(param['options'][0], str):
            param['options'] = _convert_options_strings(param['options'])
        elif 'options' in param and isinstance(
                param['options'][0], collections.Sequence):
            param['options'] = _convert_options_sequences(param['options'])


def _convert_options_strings(options):
    return [{"text": o, "value": o} for o in options]


def _convert_options_sequences(options):
    return [{"text": o[0], "value": o[1]} for o in options]


def _infer_interactive_param_type(param):
    v = param['value']
    # Boolean test must come first since bools are also integers
    if isinstance(v, bool):
        return "boolean"
    elif isinstance(v, float):
        return "float"
    elif isinstance(v, int):
        return "integer"
    elif isinstance(v, str):
        return "string"


def _convert_params_to_type(output_view_provider, params):
    method_sig = inspect.signature(output_view_provider.generate_data)
    method_params = method_sig.parameters
    # Special query parameter _meta holds type information for interactive
    # parameters (will only be present if there are interactive parameters)
    meta = json.loads(params.pop("_meta", "{}"))
    for k, v in params.items():
        meta_type = meta[k]['type'] if k in meta else None
        default_value = None
        if (k in method_params and
            method_params[k].default is not inspect.Parameter.empty and
                method_params[k].default is not None):
            default_value = method_params[k].default
        # TODO: handle lists?
        # Handle boolean and numeric values, converting from string
        if meta_type == 'boolean' or isinstance(default_value, bool):
            params[k] = v == "true"
        elif meta_type == 'float' or isinstance(default_value, float):
            params[k] = float(v)
        elif meta_type == 'integer' or isinstance(default_value, int):
            params[k] = int(v)
        elif meta_type == 'string' or isinstance(default_value, str):
            params[k] = v
        else:
            logger.warning(
                f"Unrecognized type for parameter {k}: "
                f"meta_type={meta_type}, default_value={default_value}")
    return params
