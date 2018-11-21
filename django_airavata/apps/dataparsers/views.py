from django.shortcuts import render


def home(request):
    return render(request, 'django_airavata_dataparsers/home.html')


def parser_details(request, parser_id):
    return render(request, 'django_airavata_dataparsers/parser-details.html', {
        "parser_id": parser_id
    })
