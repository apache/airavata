# Queue Settings Calculator

A _Queue Settings Calculator_ is a Python function that computes the queue
settings for an experiment. The function takes an instance of an experiment and
returns:

-   queue name
-   core count
-   node count
-   walltime limit

The Airavata Django Portal then uses these values to populate the Queue Settings
fields when users are creating/editing experiments. This can greatly simplify
experiment configuration for users and also lead to a better use of resources.

## Getting started

To add a Queue Settings Calculator function, you first will need a custom Django
App. See the [Custom Django App](./custom_django_app.md) notes for how to create
one.

Next, add a `queue_settings_calculators.py` file to your custom Django app
package if it doesn't already exist. You should add this file to the same folder
that contains the `apps.py` file.

In this file, you'll need to import the `@queue_settings_calculators` decorator.
You'll use this to mark the queue settings calculator functions (you can define
more than one).

```python
from airavata.model.experiment.ttypes import ExperimentModel
from airavata_django_portal_sdk.decorators import queue_settings_calculator

@queue_settings_calculator(
    id="gateway_name-queue-settings-for-my-app", name="My Gateway: Queue Settings for My App"
)
def my_queue_settings_calculator(request, experiment_model: ExperimentModel):
    # See https://airavata.apache.org/api-docs/master/experiment_model.html#Struct_ExperimentModel for ExperimentModel fields

    total_core_count = 4
    queue_name = "shared"
    node_count = 1
    walltime_limit = 30

    # Return a dictionary with the queue settings values
    result = {}
    result["totalCPUCount"] = total_core_count
    result["queueName"] = queue_name
    result["nodeCount"] = node_count
    result["wallTimeLimit"] = walltime_limit
    return result
```

The `id` and `name` that are passed to the `@queue_settings_calculator`
decorator are optional but highly recommended. The `id` will be used internally
to associate applications with this function. Set the id to something that is
unique to your gateway. The `name` is the value that will be displayed in the
Settings UI for selecting this queue settings calculator.

The queue settings calculator function is passed the Django `request` object and
the `experiment_model`, an
[ExperimentModel](https://airavata.apache.org/api-docs/master/experiment_model.html#Struct_ExperimentModel)
instance.

Primarily your function will inspect the `experiment_model` to determine the
appropriate queue settings. For example you might look at one of the
experiment's input files to determine how many cores are optimal for the job.

Next, add the following import line to the `ready()` function of your apps.py
AppConfig class. It will look something like this although the names of your
AppConfig class and packages will be different:

```python
class CustomDjangoAppConfig(AppConfig):
    name = 'custom_django_app'
    label = name
    verbose_name = "Custom Django App"
    fa_icon_class = "fa-comment"
    url_home = "custom_django_app:hello_world"

    def ready(self) -> None:
        from custom_django_app import queue_settings_calculators  # noqa
```

Add the `ready(self)` function to your AppConfig if it is missing. Then add an
import of your queue_settings_calculators module. Importing the module will
register the calculator functions at startup time.

## Configuring an application's queue settings calculator

To have one of your applications use your queue settings calculator, first make
sure to install the custom Django app in your portal instance. Then, go to
**Settings > Application Catalog** and click on your application. Select the
**Interface** tab. Under _Queue Settings Calculator_, select your queue settings
calculator from the drop down. It should be listed with the name that you gave
to it in your function's decorator
(`@queue_settings_calculator(id='...', name='...')`).

When an application is configured to use a queue settings calculator, the Queue
Settings UI in the Create/Edit Experiment views is disabled and made read only.

## Examples

-   <https://github.com/bio-miga/miga-autocomplete>
