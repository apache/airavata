from django.apps import AppConfig


class {{ cookiecutter.app_config_class_name }}(AppConfig):
    name = '{{ cookiecutter.project_slug }}'
    label = name
    verbose_name = "{{ cookiecutter.project_name }}"
