import os

from setuptools import find_packages, setup


def read(fname):
    with open(os.path.join(os.path.dirname(__file__), fname)) as f:
        return f.read()


setup(
    name="airavata-django-portal-sdk",
    version="1.8.4",
    url="https://github.com/apache/airavata-django-portal-sdk",
    author="Apache Software Foundation",
    author_email="dev@airavata.apache.org",
    description=(
        "The Airavata Django Portal SDK is a library that makes "
        "it easier to develop Airavata Django Portal customizations."
    ),
    long_description=read("README.md"),
    long_description_content_type='text/markdown',
    license="Apache License 2.0",
    packages=find_packages(),
    install_requires=[
        "django",
        "djangorestframework",
        "airavata-python-sdk",
        # requests 2.28 drops support for Python 3.6
        "requests < 2.28.0",
        "zipstream-new",
    ],
    classifiers=[
        "Development Status :: 4 - Beta",
        "Environment :: Web Environment",
        "Framework :: Django",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: Apache Software License",
        "Natural Language :: English",
        "Programming Language :: Python :: 3.6",
        "Topic :: Internet :: WWW/HTTP",
        "Topic :: Internet :: WWW/HTTP :: WSGI :: Application",
    ]
)
