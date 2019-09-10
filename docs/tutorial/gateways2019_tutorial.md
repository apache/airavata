# Gateways 2019 Tutorial

Objective: learn the basics of the Apache Airavata Django Portal and how to make
both simple and complex customizations to the user interface.

Prerequisites: tutorial attendees should have:

-   a laptop on which to write Python code
-   Git client

We'll install Python and Node.js as part of the tutorial.

## Outline

-   Introduction
-   Presentation: Overview of Airavata and Django Portal
    -   History of the Airavata UI and how did we get here
-   Hands on: run a basic computational experiment in the Django portal
-   Tutorial exercise: customize the input user interface for an application
-   Tutorial exercise: Create a custom output viewer for an output file
-   Tutorial exercise: Create a custom Django app
    -   use the `AiravataAPI` JavaScript library for utilizing the backend
        Airavata API
    -   develop a simple custom user interface for setting up and visualizing
        computational experiments

## Hands on: run a Gaussian computational experiment in the Django portal

### Create a portal user account

First, you'll need a user account. Go to the
[Create Account](https://pearc19.scigap.org/auth/create-account) page and select
**Sign in with existing institution credentials**. This will take you to the
CILogon institution selection page. If you don't find your institution listed
here, go back to the _Create Account_ page and fill out the form to create an
account with a username, password, etc.

After you've logged in, an administrator can grant you access to run the
Gaussian application. During the tutorial we'll grant you access right away and
let you know.

When you log in for the first time you will see a list of applications that are
available in this science gateway. Applications that you are not able to run are
greyed out but the other ones you can run. Once you are granted access, refresh
the page and you should now see that you the _Gaussian_ application is not
greyed out.

### Submit a test job

From the dashboard, click on the **Gaussian** application. The page title is
_Create a New Experiment_.

Here you can change the _Experiment Name_, add a _description_ or select a
different project is you have another project if you have multiple projects.

We'll focus on the _Application Inputs_ for this hands-on. The Gaussian
application requires one input, an _Input-File_. The following are preconfigured
Gaussian input files. Download one of these to your laptop and then click the
**Browse** button to upload the file:

-   [oxygen.inp](./data/oxygen.inp)

You can click on **View File** to take a quick look at the file.

Now we'll select what account to charge and where to run this job. The
_Allocation_ field should already be selected. Under _Compute Resource_ make
sure you select **comet.sdsc.edu**.

Then click **Save and Launch**.

You should then be taken to the _Experiment Summary_ page which will update as
the job progresses. When the job finishes you'll be able to download the `.log`
file which is the primary output file of _Gaussian_.

## Tutorial exercise: customize the input user interface for an application

For this exercise we'll define an application based on the Computational Systems
Biology Group's [_eFindSite_](http://www.brylinski.org/efindsite) drug-binding site detection software.

### Basic application configuration

1. In the portal, after you've logged in, click on the dropdown menu at the top
   (currently **Workspace** is likely selected) and select **Settings**.
2. You should see the _Application Catalog_. Click on the **New Application**
   button.
3. For _Application Name_ provide `eFindSite-<your username>`. Appending your
   username will allow you to distinguish your version of _eFindSite_ from other
   users.
4. Click **Save**.
5. Click on the **Interface** tab.
6. This application has 4 command line inputs. We'll add them now. To add the
   first one, click on **Add application input** and provide the following
   information:
    - _Name_: `Target ID`
    - _Type_: STRING (which is the default)
    - _Application Argument_: `-i`
    - _User Friendly Description_: `3-10 alphanumerical characters.`
    - _Required_: `True`
    - _Required on Command Line_: `True`
7. Add the next three application inputs in the same way, using the values in
   the table below:

| Name                  | Type   | Application Argument | Required | Required on Command Line |
| --------------------- | ------ | -------------------- | -------- | ------------------------ |
| Target Structure      | URI    | `-s`                 | True     | True                     |
| Screening libraries   | STRING | `-l`                 | False    | True                     |
| Visualization scripts | STRING | `-v`                 | False    | True                     |

(In Airavata, files are represented as URIs. When an application input has type
_URI_ it means that a file is needed for that input. From a UI point of view,
this essentially means that the user will be able to upload a file for inputs of
type URI.)

Normally we would also define the output files for this application, but for
this exercise we are only interested in exploring the options available in
customizing the application inputs and we won't actually run this application.
Likewise, we'll create a dummy deployment for this application now so that we
can invoke it from the Workspace Dashboard.

8. Click on the **Deployments** tab.
9. Click on the **New Deployment** button. Select the first compute resource in
   the drop down list and click **OK**.
10. For the _Application Executable Path_, provide the value `/usr/bin/true`.
    This is the only required field.
11. Click **Save** at the bottom of the screen.
12. Use the top level menu to go back to the **Workspace**. You should see your
    _eFindSite_ application listed there.
13. Click on your _eFindSite_ application.

If you see a form with the inputs that we registered for the application
(_Target ID_, etc.) then you have successfully register the application
interface.

### Improving the application input user interface

There are a few things to point out now:

-   the _Screening libraries_ and _Visualization scripts_ only accept specific
    values. For example, one of the allowed values for _Screening libraries_ is
    `screen_drugbank`
-   the _Target ID_ input takes a string value, but only certain characters
    (alphanumeric) are allowed and the string value has a minimum and maximum
    allowed length.

We can make this user interface more user friendly by providing more guidance in
the application inputs user interface. For the _Screening libraries_ and
_Visualization scripts_ we'll provide a list of labeled checkboxes for the user
to select. For the _Target ID_ we'll provide validation feedback that verifies
that the given value has an allowed length and only allowed characters.

1. Go back to **Settings** and in the **Application Catalog** click on your
   eFindSite application.
2. Click on the **Interface** tab.
3. For _Target ID_, in the _Advanced Input Field Modification Metadata_ box, add
   the following JSON configuration:

```json
{
    "editor": {
        "validations": [
            {
                "type": "min-length",
                "value": 3
            },
            {
                "type": "max-length",
                "value": 10
            },
            {
                "message": "Target ID may only contain alphanumeric characters and underscores.",
                "type": "regex",
                "value": "^[a-zA-Z0-9_]+$"
            }
        ],
        "ui-component-id": "string-input-editor"
    }
}
```

This JSON configuration customizes the input editor in two ways:

-   it adds 3 validations: min-length, max-length and a regex
-   it sets the UI component of the input editor to be the `string-input-editor`
    (which is also the default)

4. Likewise for _Screening Libraries_, set the _Advanced Input Field
   Modification Metadata_ to:

```json
{
    "editor": {
        "ui-component-id": "checkbox-input-editor",
        "config": {
            "options": [
                {
                    "text": "BindingDB",
                    "value": "screen_bindingdb"
                },
                {
                    "text": "ChEMBL (non-redundant, TC<0.8)",
                    "value": "screen_chembl_nr"
                },
                {
                    "text": "DrugBank",
                    "value": "screen_drugbank"
                },
                {
                    "text": "KEGG Compound",
                    "value": "screen_keggcomp"
                },
                {
                    "text": "KEGG Drug",
                    "value": "screen_keggdrug"
                },
                {
                    "text": "NCI-Open",
                    "value": "screen_nciopen"
                },
                {
                    "text": "RCSB PDB",
                    "value": "screen_rcsbpdb"
                },
                {
                    "text": "ZINC12 (non-redundant, TC<0.7)",
                    "value": "screen_zinc12_nr"
                }
            ]
        }
    }
}
```

This JSON configuration specifies a different UI component to use as the input
editor, the `checkbox-input-editor`. It also provides a list of text/value pairs
for the checkboxes; the values are what will be provided to the application as command line arguments.

5. Similarly for the _Visualization scripts_, provide the following JSON
   configuration:

```json
{
    "editor": {
        "ui-component-id": "checkbox-input-editor",
        "config": {
            "options": [
                {
                    "text": "VMD",
                    "value": "visual_vmd"
                },
                {
                    "text": "PyMOL",
                    "value": "visual_pymol"
                },
                {
                    "text": "ChimeraX",
                    "value": "visual_chimerax"
                }
            ]
        }
    }
}
```

6. Click **Save** at the bottom of the page.
7. Now, go back to the **Workspace** and on the Dashboard click on your
   _eFindSite_ application. The _application inputs_ form should now reflect
   your changes.
8. Try typing an invalid character (for example, `#`) in _Target ID_. Also try
   typing in more than 10 alphanumeric characters. When an invalid value is
   provided the validation feedback informs the user of the problem so that the
   user can correct it.

Other UI components are available:

-   textarea
-   radio buttons
-   dropdown

We're working to provide a way for custom input editors to be added by the
community, especially domain specific input editors. For example, a ball and
stick molecule editor or a map view for selecting a bounding box of a region of
interest.

Also you can define dependencies between application inputs and show or hide
inputs based on the values of other inputs.

## Tutorial exercise: Create a custom output viewer for an output file

By default, the Django portal provides a very simple view for output files that
allows users to download the file to their local machine. However, it is
possible to provide additional custom views for output files. Examples include:

-   image (visualization)
-   link (perhaps to another web application that can visualize the file)
-   chart
-   parameterized notebook

To be able to create a custom output viewer we'll need to write some Python
code. First, we'll get a local version of the Django portal running which we'll
use as a developer environment.

### Setup local Django portal development environment

1. Make sure you have Python 3.6+ installed. See
   [https://www.python.org/downloads/]() for downloadable packages or use your
   system's package manager.
2. You'll also need npm 6.4.1+ to build the JavaScript frontend code. Please
   install
   [the most recent LTS version of Node.js](https://nodejs.org/en/download/) or
   use your system's package manager.
3. Clone the airavata-django-portal project and create a virtual environment.

```bash
git clone https://github.com/apache/airavata-django-portal.git
cd airavata-django-portal
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

4. Now we'll clone another repository that has some supporting files for this
   tutorial. Change into the parent directory and clone
   [https://github.com/machristie/gateways19-tutorial]()

```bash
cd ..
git clone https://github.com/machristie/gateways19-tutorial.git
```

5. Copy the `settings_local.py` file from this repo into the Django portal repo:

```bash
cp gateways19-tutorial/settings_local.py airavata-django-portal/django_airavata/
```

6. Back in the Django portal repo we'll run the Django migrate command.

```bash
cd airavata-django-portal
python manage.py migrate
```

7. Load a starting set of CMS pages for the portal

```bash
python manage.py load_default_gateway
```

8. Build the JavaScript sources for the portal. This one will take some time to
   complete.

```bash
./build_js.sh
```

Once the build finishes we can start the Django server and log in and see our
experiments.

```bash
export OAUTHLIB_INSECURE_TRANSPORT=1
python manage.py runserver
```

Go to [http://localhost:8080](), click on **Login in**, enter your username and
password. On the dashboard you should see the your experiments listed on the
right hand side.

### Setup the custom output viewer package

1. Change back into the _gateways19-tutorial_ directory and install it into the
   Django portal's virtual environment. Make sure you still have the Django
   portal's virtual environment activated; your terminal prompt should start
   with `(venv)`. If the Django portal virtual environment isn't activated, see
   step 3 in the previous section. We'll also use pip to install output viewer's
   dependencies.

```bash
cd ../gateways19-tutorial
pip install -r requirements.txt
python setup.py develop
```

2. Implement the GaussianLogViewProvider in output_views.py. First we'll add
   some imports

```python
import io

import numpy as np
from matplotlib.figure import Figure
```

3. Next we'll define the GaussianLogViewProvider class, set it's `display_type`
   to _image_ and give it a name:

```python
class GaussianLogViewProvider:
    display_type = 'image'
    name = "Gaussian Log Viewer"
```

4. Now we'll implement the `generate_data` function. This function should return
   a dictionary with values that are expected for this `display_type`. For a
   display type of _image_, the required return values are _image_ which should
   be a bytes array or file-like object with the image bytes and _mime-type_
   which should be the image's mime type. Here's the `generate_data` function:

```python
    def generate_data(self, request, experiment_output, experiment, output_file=None):
        # return dictionary with image data
        N = 500
        x = np.random.rand(N)
        y = np.random.rand(N)
        fig = Figure()
        ax = fig.subplots()
        ax.scatter(x, y)
        ax.set_title('Random scatterplot')
        ax.set_xlabel('x')
        ax.set_ylabel('y')
        buffer = io.BytesIO()
        fig.savefig(buffer, format='png')
        image_bytes = buffer.getvalue()
        buffer.close()
        return {
            'image': image_bytes,
            'mime-type': 'image/png'
        }
```

5. Altogether, the output_views.py file should have the following contents:

```python
import io

import numpy as np
from matplotlib.figure import Figure

class GaussianLogViewProvider:
    display_type = 'image'
    name = "Gaussian Log Viewer"

    def generate_data(self, request, experiment_output, experiment, output_file=None):
        # return dictionary with image data
        N = 500
        x = np.random.rand(N)
        y = np.random.rand(N)
        fig = Figure()
        ax = fig.subplots()
        ax.scatter(x, y)
        ax.set_title('Random scatterplot')
        ax.set_xlabel('x')
        ax.set_ylabel('y')
        buffer = io.BytesIO()
        fig.savefig(buffer, format='png')
        image_bytes = buffer.getvalue()
        buffer.close()
        return {
            'image': image_bytes,
            'mime-type': 'image/png'
        }
```

6. Now we need to register our _output view provider_ with the package metadata
   so that the Django Portal will be able to discover it. Add the following
   lines to the `entry_points` parameter in the `setup.py` file:

```python
setuptools.setup(
# ...
    entry_points="""
[airavata.output_view_providers]
gaussian-log-image = gateways19_tutorial.output_views:GaussianLogViewProvider
""",
)
```

`gaussian-log-image` is the output view provider id.
`gateways19_tutorial.output_views` is the module in which the
`GaussianLogViewProvider` output view provider class is found.

7. Since we've updated the `entry_points` metadata, we need to reinstall this
   package in the Django Portal's virtual environment.

```bash
# Activate the airavata-django-portal virtual environment if not already activated
cd ../airavata-django-portal
source venv/bin/activate
cd ../gateways19-tutorial
python setup.py develop
```

### Use the GaussianLogViewProvider with the Gaussian log output file

Back in the Django Portal, we'll update the application interface for Gaussian
to add the GaussianLogViewProvider as an additional output view of the file.

1. Log into your local Django Portal instance.
2. In the menu at the top, select **Settings**.
3. Click on the **Gaussian16** application.
4. Click on the **Interface** tab.
5. Scroll down to the _Output Field: Gaussian-Application-Output_.
6. Add the following in the _Metadata_ section:

```json
{
    "output-view-providers": ["gaussian-log-image"]
}
```

7. Click **Save**.
8. Go back to the **Workspace** using the menu at the top.
9. Select your Gaussian16 experiment.
10. For the .log output file there should be a dropdown menu allowing you to
    select an alternate view. Select **Gaussian Log Viewer**. Now you should see
    the image generated by the custom output view provider.

## Tutorial exercise: Create a custom Django app

In this tutorial exercise we'll create a fully custom user interface that lives
within the Django Portal.

What we're going to build is a very simple user interface that will:

-   allow a user to pick a greeting in one of several languages
-   submit a simple _echo_ job to a batch scheduler to echo that greeting
-   display the echoed greeting by displaying the STDOUT file produced by the
    job

### Setting up the Django app

To start, we'll just create a simple "Hello World" page for the Django app and
get it properly registered with the local Django Portal instance.

1. In the `gateways19-tutorial`, create a file with the path
   `gateways19_tutorial/templates/gateways19_tutorial/hello.html` with the
   following contents:

```xml
{% extends 'base.html' %}

{% block content %}
<div class="main-content-wrapper">
    <main class="main-content">
        <h1>Hello World</h1>
    </main>
</div>
{% endblock content %}
```

2. Create a file with the path `gateways19_tutorial/apps.py` with the following
   contents:

```python
from django.apps import AppConfig


class Gateways19TutorialAppConfig(AppConfig):
    name = 'gateways19_tutorial'
    label = name
    verbose_name = "Gateways 19 Tutorial"
    fa_icon_class = "fa-comment"
```

3. Create a file with the path `gateways19_tutorial/views.py` with the following
   contents:

```python
from django.shortcuts import render
from django.contrib.auth.decorators import login_required


@login_required
def hello_world(request):
    return render(request, "gateways19_tutorial/hello.html")
```

4. Create a file with the path `gateways19_tutorial/urls.py` with the following
   contents:

```python
from django.conf.urls import url, include

from . import views

app_name = 'gateways19_tutorial'
urlpatterns = [
    url(r'^hello/', views.hello_world, name="home"),
]
```

5. We've created the necessary code for our Django app to display the hello
   world page, but now we need to add some metadata so that the Django Portal
   knows about this Django app. In `setup.py`, add the following to the
   entry_points section:

```python
setuptools.setup(
# ...
    entry_points="""
[airavata.output_view_providers]
gaussian-log-image = gateways19_tutorial.output_views:GaussianLogViewProvider
[airavata.djangoapp]
gateways19_tutorial = gateways19_tutorial.apps:Gateways19TutorialAppConfig
""",
)
```

6. Since we've updated the metadata, we need to install it again into the Django
   Portal's virtual environment. Make sure that the Django Portal's virtual
   environment is activated and run:

```bash
# Activate the airavata-django-portal virtual environment if not already activated
cd ../airavata-django-portal
source venv/bin/activate
cd ../gateways19-tutorial
python setup.py develop
```

7. Start the Django Portal server again:

```bash
cd ../airavata-django-portal
export OAUTHLIB_INSECURE_TRANSPORT=1
python manage.py runserver
```

Now you should be able to log into the portal locally and see **Gateways 19
Tutorial** in the drop down menu in the header (click on **Workspace** then you
should see it in that menu).

### Adding a list of "Hello" greetings
