from django.shortcuts import render


def home(request):

    request.active_nav_item = "manage"
    return render(request, 'django_airavata_dataparsers/parsers-manage.html', {
        "bundle_name": "parser-list"
    })


def parser_details(request, parser_id):
    return render(request, 'django_airavata_dataparsers/parser-details.html', {
        "parser_id": parser_id,
        "bundle_name": "parser-details"
    })


def edit_parser(request, parser_id):
    return render(request, 'django_airavata_dataparsers/edit-parser.html', {
        "parser_id": parser_id,
        "bundle_name": "parser-edit"
    })
