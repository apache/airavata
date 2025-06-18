#!/usr/bin/env python
# This script based on
# https://docs.djangoproject.com/en/1.11/topics/testing/advanced/#using-the-django-test-runner-to-test-reusable-applications
import os
import sys

import django
from django.conf import settings
from django.test.utils import get_runner

if __name__ == "__main__":
    os.environ['DJANGO_SETTINGS_MODULE'] = 'tests.settings'
    django.setup()
    TestRunner = get_runner(settings)
    test_runner = TestRunner()
    # Pass None for test_labels to have the runner discover all tests under
    # this directory
    failures = test_runner.run_tests(None)
    sys.exit(bool(failures))
