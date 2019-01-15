from django.shortcuts import render
from rest_framework.renderers import JSONRenderer

from django_airavata.apps.api.views import (
    ParserViewSet
)

def home(request):

    request.active_nav_item = "manage"
    response = ParserViewSet.as_view({'get': 'list'})(request)
    parsers_json = JSONRenderer().render(response.data)
    return render(request, 'django_airavata_dataparsers/parsers-manage.html')

def parser_details(request, parser_id):
    return render(request, 'django_airavata_dataparsers/parser-details.html', {
        "parser_id": parser_id
    })

def edit_parser(request, parser_id):
    return render(request, 'django_airavata_dataparsers/edit-parser.html', {
        "parser_id": parser_id
    })
