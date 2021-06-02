{% if cookiecutter.output_view_display_type == "image" %}
import io
{% elif cookiecutter.output_view_display_type == "html" %}
from django.template.loader import render_to_string
{% endif %}

class {{ cookiecutter.output_view_provider_class_name }}:
    display_type = "{{ cookiecutter.output_view_display_type }}"
    # As a performance optimization, the output view provider can be invoked
    # immediately instead of only after being selected by the user in the
    # portal.  Set to True to invoke immediately. Only use this with simple
    # output view providers that return quickly
    immediate = False
    name = "{{ cookiecutter.project_name }}"

    def generate_data(self, request, experiment_output, experiment,{% if "single" in cookiecutter.number_of_output_files %} output_file=None,{% else %} output_files=None,{% endif %} **kwargs):
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
