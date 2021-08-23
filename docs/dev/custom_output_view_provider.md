# Custom Output View Provider

A custom _output view provider_ generates visualizations of experiment outputs.
Output view providers are implemented as a Python function and packaged as a
Python package, with the requisite metadata (more on this below). An output view
provider is associated with the output of an application in the Application
Catalog.

There are several different output view display types, such as: image, link, or
html. If configured an output view will be displayed for an output file and the
Airavata Django Portal will invoke the custom output view provider to get the
data to display. For example, if the output view display type is image, then the
output view provider will be invoked and it should return image data.

## Getting started

For a step by step tutorial approach to creating a custom output view provider,
see
[Custom UI tutorial](../tutorial/custom_ui_tutorial.md#tutorial-exercise-create-a-custom-output-viewer-for-an-output-file).

To create a custom output view provider, we'll need to create an installable
Python package with the required metadata to describe the custom output view
provider and how to import it. We'll use a project code generation tool called
[Cookiecutter](https://cookiecutter.readthedocs.io/) to generate all of the
necessary files and configuration. These steps will show how to use
Cookiecutter.

1.  Install the Airavata Django Portal if you haven't already. See the
    [https://github.com/apache/airavata-django-portal/blob/master/README.md](README)
    for instructions.
2.  With the Airavata Django Portal virtual environment activated, install
    cookiecutter.

```
pip install -U cookiecutter
```

3.  If you haven't already, you'll need to create a custom Django app to contain
    your output view provider code. If you already have a custom Django app,
    skip to step 4. To create a custom Django app, navigate to a separate
    directory outside the airavata-django-portal, where you'll create your
    custom django app. Use cookiecutter to run the Airavata Django app template.

```
cookiecutter https://github.com/machristie/cookiecutter-airavata-django-app.git
```

You'll need to answer some questions. The project name is the most important
one. You can name it whatever you want. For these instructions I'll name it
**Custom Django App**. For the rest of the questions, you can simply accept the
defaults:

```
project_name [My Custom Django App]: Custom Django App
project_slug [custom_django_app]:
project_short_description [Custom Django app with everything needed to be installed in the airavata-django-portal]:
app_config_class_name [CustomDjangoAppConfig]:
version [0.1.0]:
```

See [Adding a Custom Django App](./custom_django_app.md) for more details.

4.  Change into the directory of your custom Django app. Then run the following
    command:

```
cd custom_django_app  # Or whatever you named your custom Django app
cookiecutter https://github.com/machristie/cookiecutter-airavata-django-output-view.git -f
```

You'll need to answer some questions. For `project_name` give the name for your
output view provider. The other very important question is the
`custom_django_app_module_name`. This should be the module name of your custom
Django app. In the running example, I would put `custom_django_app`. If you get
an error about the custom_django_app_module_name, check the error message
because it should provide a hint for what you should provide instead.

The other questions are hopefully self explanatory and for most you can just
accept the default value. You'll have the option of generating an image, html or
link type output view provider. Also, you'll be asked whether your output view
provider is to be used with a single file (URI) or a collection of files
(URI_COLLECTION); if you're not sure, just pick the default value for now.

5.  To install the custom Django app and the generated output view provider, run
    the following in your custom Django app directory (the directory with the
    setup.cfg file):

```
pip install -e .
```

### Generated files

The output view provider cookiecutter does two things:

1. it generates a output view provider file in the `output_views/` folder
2. add an entry point listing to entry_points in `setup.cfg`

See the output view provider file for the `generate_data` function. This is
where you'll add your code. There is some commented out sample code to show you

-   how to read the output file
-   how to do more advanced interactions with the user's storage using the
    user_storage module of the Airavata Django Portal SDK
-   how to call the Airavata API using the Airavata Python SDK
-   how to create the returned dictionary with the expected values for the given
    display type (for example, for 'image' display type, the returned dictionary
    should contain a key called `image` with the bytes of the images and a key
    called `mime-type` with the mime type of the image).

The rest of the documentation provides additional reference and guidance on
implementing the `generate_data` function.

### Setting up remote data access

To access the files in the remote deployed Django portal instance in your local
development environment you need to configure a setting so that your local
Django instance knows at what URL is the remote deployed Django portal REST API.
The remote API will be used for accessing data, making your local instance
behave just like the remote instance. Set the GATEWAY_DATA_STORE_REMOTE_API in
settings_local.py to have the domain of the remote deployed Django portal:

```
# Change this to match your remote Django portal instance
GATEWAY_DATA_STORE_REMOTE_API = 'https://testdrive.airavata.org'
```

## Reference

### Output View Provider interface

Output view providers should be defined as a Python class. They should define
the following attributes:

-   `display_type`: this should be one of _link_, _image_ or _html_.
-   `name`: this is the name of the output view provider displayed to the user.

Optional attributes that can be defined on the output view provider class
include:

-   `test_output_file`: this is a file path to an file that will be substituted
    for the actual file for testing the output view provider. This is only used
    during development and will only work with the Django DEBUG setting is True.
    For more information, see
    [Using test_output_file in development](#using-test_output_file-in-development).

The output view provider class should define the following method:

```python
def generate_data(self, request, experiment_output, experiment, output_file=None, **kwargs):

    # Return a dictionary
    return {
        #...
    }
```

For the output view provider to work with experiment outputs of type
URI_COLLECTION, add `output_files=None` to the function signature and get the
output as a list of file objects.

```python
# For URI_COLLECTION, add output_files=None to signature
def generate_data(self, request, experiment_output, experiment, output_files=None, **kwargs):

    # Return a dictionary
    return {
        #...
    }
```

The arguments to the `generate_data` function are described below:

-   `request` -
    [Django Request](https://docs.djangoproject.com/en/dev/ref/request-response/#django.http.HttpRequest)
    object.
-   `experiment_output` - Airavata metadata about output file(s), see
    [OutputDataObjectType doc](http://airavata.apache.org/api-docs/master/application_io_models.html#Struct_OutputDataObjectType).
-   `experiment` - Airavata metadata about the experiment, see
    [ExperimentModel doc](http://airavata.apache.org/api-docs/master/experiment_model.html#Struct_ExperimentModel).
-   `output_file` - Python
    [file-like object](https://docs.python.org/3/glossary.html#term-file-object).
    Read from this file to process the contents of the output file.
-   `output_files` - If the output type is URI_COLLECTION, then the collection
    of files is given as a list of file-like objects.

The required contents of the dictionary varies based on the _display type_.

#### Display type link

The returned dictionary should include the following entries:

-   url
-   label

The _label_ is the text of the link. Generally speaking this will be rendered
as:

```html
<a href="{{ url }}">{{ label }}</a>
```

**Examples**

-   [SimCCS Maptool - SolutionLinkProvider](https://github.com/SciGaP/simccs-maptool/blob/master/simccs_maptool/output_views.py#L5)

#### Display type image

The returned dictionary should include the following entries:

-   image: a stream of bytes, i.e., either the result of `open(file, 'rb')` or
    something equivalent like `io.BytesIO`.
-   mime-type: the mime-type of the image, for example, `image/png`.

**Examples**

-   [AMP Gateway - TRexXPlotViewProvider](https://github.com/SciGaP/amp-gateway-django-app/blob/master/amp_gateway/plot.py#L115)

#### Display type html

The returned dictionary should include the following entries:

-   output: a raw HTML string
-   js: a static URL to a JavaScript file, for example,
    `/static/earthquake_gateway/custom-leaflet-script.js`.

**Examples**

-   [dREG - DregGenomeBrowserViewProvider](https://github.com/SciGaP/dreg-djangoapp/blob/gbrowser/dreg_djangoapp/output_views.py)

### Entry Point registration

Custom output view providers are packaged as Python packages in order to be
deployed into an instance of the Airavata Django Portal. The Python package must
have metadata that indicates that it contains a custom output view provider.
This metadata is specified as an _entry point_ in the package's `setup.py` file
under the named parameter `entry_points`.

The entry point must be added to an entry point group called
`[airavata.output_view_providers]`. The entry point format is:

```
label = module:class
```

The _label_ is the identifier you will use when associating an output view
provider with an output file in the Application Catalog. As such, you can name
it whatever you want. The _module_ must be the Python module in which exists
your output view provider. The _class_ must be the name of your output view
provider class.

See the **Getting Started** section for an example of how to format the entry
point in `setup.py`.

### Associating an output view provider with an output file

In the Application Catalog, you can add JSON metadata to associate an output
view provider with an output file.

1. In the top navigation menu in the Airavata Django Portal, go to **Settings**.
2. If not already selected, select the **Application Catalog** from the left
   hand side navigation.
3. Click on the application.
4. Click on the **Interface** tab.
5. Scroll down to the _Output Fields_ and find the output file with which you
   want to associate the output view provider.
6. In the _Metadata_ field, add or update the `output-view-providers` key. The
   value should be an array (beginning and ending with square brackets). The
   name of your output view provider is the label you gave it when you created
   the entry point.

The _Metadata_ field will have a value like this:

```json
{
    "output-view-providers": ["gaussian-eigenvalues-plot"]
}
```

Where instead of `gaussian-eigenvalues-plot` you would put or add the label of
your custom output view provider.

There's a special `default` output view provider that provides the default
interface for output files, namely by providing a download link for the output
file. This `default` output view provider will be shown initially to the user
and the user can then select a custom output view provider from a drop down
menu. If, instead, you would like your custom output view provider to be
displayed initially, you can add the `default` view provider in the list of
output-view-providers and place it second. For example:

```json
{
    "output-view-providers": ["gaussian-eigenvalues-plot", "default"]
}
```

would make the `gaussian-eigenvalues-plot` the initial output view provider. The
user can access the default output view provider from the drop down menu.

### Accessing additional experiment output files

The output view provider is associated with a particular output file, but your
output view provider can access other files in the experiment data directory. To
access those files use the `list_experiment_dir` of the
[user_storage module](https://airavata-django-portal-sdk.readthedocs.io/en/latest/#module-user_storage)
in the Airavata Django Portal SDK.

```python
from airavata_django_portal_sdk import user_storage
def generate_data(self, request, experiment_output, experiment, output_file=None, **kwargs):

    dirs, files = user_storage.list_experiment_dir(request, experiment.experimentId)
    # ...
```

`list_experiment_dir` returns a tuple of directories and files in the experiment
data directory. Each entry is a dictionary of metadata about the directory/file.
See the SDK documentation for more information.

### Using test_output_file in development

The output view provider class can specify a `test_output_file` attribute. The
value should be the file path to a sample output file within the output view
provider's Python package. For an example of how to set `test_output_file`, see
[this example](https://github.com/machristie/gateways19-tutorial/blob/044d4c6ddda48e7d0fa17f6c1d84936919c9303c/gateways19_tutorial/output_views.py#L14).
This file will substitute for the real experiment output file when the Django
DEBUG setting is True and the output view provider request is made in _test
mode_. This can be used to develop the output view provider with a sample output
file when access to an actual experiment generated output file is not easily
available (see [Setting up remote data access](#setting-up-remote-data-access)
for information on using experiment outputs in your local development
environment if experiment generated output files are an option for you).

To enable _test mode_, you have two options. First, you can test the output view
provider REST API directly and add a query parameter of `test-mode=true`:

    http://localhost:8000/api/image-output?experiment-id=...expid...&experiment-output-name=Gaussian-Application-Output&provider-id=gaussian-eigenvalues-plot&test-mode=true

But substitute your experiment id, etc and change `image-output` to
`html-output` or `link-output` or whatever display type is appropriate for your
output view provider. You can load the output view in the Airavata Django Portal
and then in your browser's developer tools, find the REST API request, open it
in a new tab and change the test-mode value to `true`.

Second, you can modify the output view provider UI to always pass
`test-mode=true` when making REST API requests to load data from output view
providers. To do this, open the OutputViewDataLoader.js file and change
TEST_MODE to `true`:

```javascript
const TEST_MODE = true;
```

Then in `django_airavata/apps/workspace` run `yarn && yarn build` (or
`yarn && yarn serve` if you are developing frontend code).

### Interactive parameters

You can add some interactivity to your custom output view provider by adding one
or more interactive parameters. An interactive parameter is a parameter that
your custom output view provider declares, with a name and current value. The
Airavata Django Portal will display all interactive parameters in a form and
allow the user to manipulate them. When an interactive parameter is updated by
the user, your custom output view provider will be again invoked with the new
value of the parameter.

To add an interactive parameter, you first need to add a keyword parameter to
your `generate_data` function. For example, let's say you want to add a boolean
`show_grid` parameter that the user can toggle on and off. You would change the
signature of the `generate_data` function to:

```python
def generate_data(self, request, experiment_output, experiment, output_file=None, show_grid=False, **kwargs):

    # Return a dictionary
    return {
        #...
    }
```

In this example, the default value of `show_grid` is `False`, but you can make
it `True` instead. The default value of the interactive parameter will be its
value when it is initially invoked. It's recommended that you supply a default
value but the default value can be `None` if there is no appropriate default
value.

Next, you need to declare the interactive parameter in the returned dictionary
along with its current value in a special key called `interactive`. For example:

```python
def generate_data(self, request, experiment_output, experiment, output_file=None, show_grid=False, **kwargs):

    # Return a dictionary
    return {
        #...
        'interactive': [
            {'name': 'show_grid', 'value': show_grid}
        ]
    }
```

declares the interactive parameter named `show_grid` and its current value.

The output view display will render a form showing the value of `show_grid` (in
this case, since it is boolean, as a checkbox).

#### Supported parameter types

Besides boolean, the following additional parameter types are supported:

| Type    | UI Control              | Additional options                                                                                          |
| ------- | ----------------------- | ----------------------------------------------------------------------------------------------------------- |
| Boolean | Checkbox                |                                                                                                             |
| String  | Text input              |                                                                                                             |
| Integer | Stepper or Range slider | `min`, `max` and `step` - if `min` and `max` are supplied, renders as a range slider. `step` defaults to 1. |
| Float   | Stepper or Range slider | `min`, `max` and `step` - if `min` and `max` are supplied, renders as a range slider.                       |

Further, if the interactive parameter defines an `options` list, this will
render as a drop-down select. The `options` list can either be a list of
strings, for example:

```python
def generate_data(self, request, experiment_output, experiment, output_file=None, color='red', **kwargs):

    # Return a dictionary
    return {
        #...
        'interactive': [
            {'name': 'color', 'value': color, 'options': ['red', 'green', 'blue']}
        ]
    }
```

Or, the `options` list can be a list of `(text, value)` tuples:

```python
def generate_data(self, request, experiment_output, experiment, output_file=None, color='red', **kwargs):

    # Return a dictionary
    return {
        #...
        'interactive': [
            {'name': 'color', 'value': color, 'options': [('Red', 'red'), ('Blue', 'blue'), ('Green', 'green')]}
        ]
    }
```

The `text` is what is displayed to the user as the value's label in the
drop-down. The `value` is what will be passed to the output view provider when
selected by the user.

#### Additional configuration

The following additional properties are supported:

-   **label** - by default the name of the interactive parameter is its label in
    the interactive form. You can customize the label with the `label` property.
-   **help** - you can also display help text below the parameter in the
    interactive form with the `help` property.

For example:

```python
def generate_data(self, request, experiment_output, experiment, output_file=None, color='red', **kwargs):

    # Return a dictionary
    return {
        #...
        'interactive': [
            {'name': 'color',
            'value': color,
            'options': [('Red', 'red'), ('Blue', 'blue'), ('Green', 'green')],
            'label': 'Bar chart color',
            'help': 'Change the primary color of the bar chart.'}
        ]
    }
```
