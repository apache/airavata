from airavata.model.experiment.ttypes import ExperimentModel
from airavata_django_portal_sdk.decorators import queue_settings_calculator

# See https://apache-airavata-django-portal.readthedocs.io/en/latest/dev/queue_settings_calculator/ for more information
@queue_settings_calculator(
    id="{{ cookiecutter.project_slug}}-my-queue-settings-calculator", name="{{ cookiecutter.project_name}}: My Queue Settings Calculator"
)
def my_queue_settings_calculator(request, experiment_model: ExperimentModel):
    # See https://airavata.apache.org/api-docs/master/experiment_model.html#Struct_ExperimentModel for ExperimentModel fields

    # TODO: Implement logic here to determine appropriate queue settings for experiment_model
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
