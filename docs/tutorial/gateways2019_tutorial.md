# Gateways 2019 Tutorial

Objective: learn the basics of the Apache Airavata Django Portal and how to make
both simple and complex customizations to the user interface.

Prerequisites: tutorial attendees should have:

- a laptop on which to write Python code
- Git client

We'll install Python and Node.js as part of the tutorial.

## Outline

- Introduction
- Presentation: Overview of Airavata and Django Portal
  - History of the Airavata UI and how did we get here
- Hands on: run a basic computational experiment in the Django portal
- Tutorial exercise: customize the input user interface for an application
- (Optional) Tutorial exercise: Create a custom web component to customize the
  input interface
- Tutorial exercise: Create a custom output viewer for an output file
- Tutorial exercise: Create a custom Django app
  - use the `AiravataAPI` JavaScript library for utilizing the backend Airavata
    API
  - develop a simple custom user interface for setting up and visualizing
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

- [oxygen.inp](./data/oxygen.inp)

You can click on **View File** to take a quick look at the file.

Now we'll select what account to charge and where to run this job. The
_Allocation_ field should already be selected. Under _Compute Resource_ make
sure you select **comet.sdsc.edu**.

Then click **Save and Launch**.

You should then be taken to the _Experiment Summary_ page which will update as
the job progresses. When the job finishes you'll be able to download the `.log`
file which is the primary output file of _Gaussian_.

## Tutorial exercise: customize the input user interface for an application

For the exercise we'll define an application based on the _Quantum Espresso_
quantum chemistry software suite.

**TODO**: instructions on defining the application. Maybe only define the
application inputs.

From the dashboard we can now run this application. Notice that the user needs
to type in a string value to provide the module to run which isn't very user
friendly and it's error prone. Let's change this to a set of radio buttons.

Go back to the **Settings** then click on your application.

Click on the **Interface** tab.

For the **QE-App-Module** input field add the following to the _Advanced Input
Field Modification Metadata_ field:

**TODO**: more descriptive text values?

```json
{
  "editor": {
    "ui-component-id": "radio-button-input-editor",
    "config": {
      "options": [
        {
          "value": "pw",
          "text": "PW"
        },
        {
          "value": "pp",
          "text": "PP"
        },
        {
          "value": "bands",
          "text": "Bands"
        },
        {
          "value": "tlanczos",
          "text": "TLanczos"
        },
        {
          "value": "tdavidson,",
          "text": "TDavidson"
        },
        {
          "value": "neb",
          "text": "Neb"
        },
        {
          "value": "ph",
          "text": "PH"
        }
      ]
    }
  }
}
```

This configures this input field to display as a set of radio buttons and the
`options` key provides an array of values and text to be displayed for each.
Feel free to add another option.

Now go back to the **Workspace** and click on your application. Notice that the
_QE-App-Module_ field displays as radio buttons now.

Other UI components are available:

- textarea
- checkboxes
- dropdown

We're working to provide a way for custom input editors to be added by the
community, especially domain specific input editors. For example, a ball and
stick molecule editor or a map view for selecting a bounding box of a region of
interest.

In addition to customizing the UI component you can also apply validations:

- min length
- max length
- regular expression
- ... and more can be easily added

Also you can define dependencies between application inputs and show or hide
inputs based on the values of other inputs.

## (Optional) Tutorial exercise: Create a custom UI component to customize input interface

TBD

## Tutorial exercise: Create a custom output viewers for an output file

By default, the Django portal provides a very simple view for output files that
allows users to download the file to their local machine. However, it is
possible to provide additional custom views for output files. Examples include:

- image (visualization)
- link (perhaps to another web application that can visualize the file)
- chart
- parameterized notebook

To be able to create a custom output viewer we'll need to write some Python
code. First, we'll get a local version of the Django portal running which we'll
use as a developer environment.

1. Make sure you have Python 3.6+ installed. See
   [https://www.python.org/downloads/]() for downloadable packages.
2. You'll also need npm 6.4.1+ to build the JavaScript frontend code. Please
   install
   [the most recent LTS version of Node.js](https://nodejs.org/en/download/).
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
