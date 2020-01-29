#!/bin/bash

python manage.py migrate
python manage.py load_default_gateway
exec python manage.py runserver 0.0.0.0:8000
