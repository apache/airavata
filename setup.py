import os

from setuptools import find_packages, setup


def read(fname):
    with open(os.path.join(os.path.dirname(__file__), fname)) as f:
        return f.read()


setup(
    name='airavata-django-portal',
    version='0.1',
    url='https://github.com/apache/airavata-django-portal',
    author='Apache Software Foundation',
    author_email='dev@airavata.apache.org',
    description=('The Airavata Django Portal is a web interface to the '
                 'Apache Airavata API implemented using the Django web '
                 'framework.'),
    long_description=read('README.md'),
    license='Apache License 2.0',
    packages=find_packages(),
    install_requires=[
            'Django',
            'djangorestframework',
            'requests',
            'requests-oauthlib',
            'thrift',
            'thrift_connector',
            'wagtail',
            'wagtailfontawesome',
            'jupyter',
            'papermill',
            "airavata-django-portal-sdk",
    ],
    extras_require={
        'dev': [
            'flake8',
            'flake8-isort'
        ],
        'mysql': [
            'mysqlclient'
        ]
    },
    classifiers=[
        'Development Status :: 4 - Beta',
        'Environment :: Web Environment',
        'Framework :: Django',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: Apache Software License',
        'Natural Language :: English',
        'Programming Language :: Python :: 3.6',
        'Topic :: Internet :: WWW/HTTP',
        'Topic :: Internet :: WWW/HTTP :: WSGI :: Application'
    ]
)
