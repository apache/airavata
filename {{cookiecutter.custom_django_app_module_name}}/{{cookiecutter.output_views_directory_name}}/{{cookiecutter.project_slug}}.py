{% if cookiecutter.output_view_display_type == "image" %}
import io
{% elif cookiecutter.output_view_display_type == "html" %}
from django.template.loader import render_to_string
{% endif %}

from airavata_django_portal_sdk import user_storage


class {{ cookiecutter.output_view_provider_class_name }}:
    display_type = "{{ cookiecutter.output_view_display_type }}"
    # As a performance optimization, the output view provider can be invoked
    # immediately instead of only after being selected by the user in the
    # portal.  Set to True to invoke immediately. Only use this with simple
    # output view providers that return quickly
    immediate = False
    name = "{{ cookiecutter.project_name }}"

    def generate_data(self, request, experiment_output, experiment,{% if "single" in cookiecutter.number_of_output_files %} output_file=None,{% else %} output_files=None,{% endif %} **kwargs):

        # Use `output_file` or `output_files` to read from the output file(s).
        # See https://docs.python.org/3/tutorial/inputoutput.html#methods-of-file-objects
        # for how to read from file objects. For example, to read the entire file, use:
        #
        # entire_file = output_file.read()


        # Example code: user_storage module
        # To find other files in the experiment data directory, use the
        # user_storage module of the airavata_django_portal_sdk. You can use the
        # following to list the files and directories in the experiment data
        # directory:
        #
        # dirs, files = user_storage.list_experiment_dir(request, experiment.experimentId)
        #
        # The 'files' variable is a list of dictionaries, each one will have a
        # 'data-product-uri' key. Use the data-product-uri to open the file:
        #
        # data_product_uri = files[0]['data-product-uri']
        # data = user_storage.open_file(request, data_product_uri=data_product_uri)
        #
        # See https://airavata-django-portal-sdk.readthedocs.io/en/latest/#module-user_storage
        # for more information.


        # Example code: Airavata API client
        # Make calls to the Airavata API from the output view provider, for example:
        #
        # data_product = request.airavata_client.getDataProduct(
        #        request.authz_token, experiment_output.value)
        #
        # In this example, the DataProduct object is loaded for the output file.
        # 'experiment_output.value' has the Data Product URI for the output
        # file, the unique identifier in the Airavata API for this output file.
        # The returned DataProduct object contains metadata about the output
        # file and the location(s) where it is stored. See
        # http://airavata.apache.org/api-docs/master/replica_catalog_models.html#Struct_DataProductModel
        # for more information.
        #
        # The authorization token is always the first argument of Airavata API calls
        # and is available as 'request.authz_token'. Some API methods require a
        # 'gatewayID' argument and that is available on the Django settings object
        # as 'settings.GATEWAY_ID'.
        # For documentation on other Airavata API methods, see
        # https://docs.airavata.org/en/master/technical-documentation/airavata-api/.
        # The Airavata Django Portal uses the Airavata Python Client SDK:
        # https://github.com/apache/airavata/tree/master/airavata-api/airavata-client-sdks/airavata-python-sdk


    {% if cookiecutter.output_view_display_type == "link" %}
        label = "Link to Google"
        url = "https://google.com"
        return {
            "label": label,
            "url": url
        }
    {% elif cookiecutter.output_view_display_type == "image" %}
        # Typical thing is to write an image to an in-memory BytesIO object and
        # then return its bytes
        buffer = io.BytesIO()
        # Example: say you have a figure object, which is an instance of
        # matplotlib's Figure. Then you can write it to the BytesIO object
        # figure.savefig(buffer, format='png')
        image_bytes = buffer.getvalue()
        buffer.close()
        return {
            'image': image_bytes,
            'mime-type': 'image/png'
        }
    {% elif cookiecutter.output_view_display_type == "html" %}
        # Return a dictionary with 'output' as the HTML string and 'js' as the
        # absolute URL to a JavaScript file to load for the view
        # In the example code, the HTML is produced from a Django template, but
        # you don't have to do it that way.
        html_context = {}  # extra context
        html_string = render_to_string('path/to/template.html', html_context)
        js_abs_path = "/static/path/to/script.js"
        return {
            'output': html_string,
            'js': js_abs_path
        }
    {% endif %}
