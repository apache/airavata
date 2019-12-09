from setuptools import setup, find_packages

setup(
    name='airavata-python-sdk',
    version='1.0.0',
    packages=find_packages(include=['clients']),
    url='http://airavata.com',
    license='Apache License 2.0',
    author='Airavata Developers',
    author_email='dev@airavata.apache.org',
    description='Apache Airavata Python  SDK'
)
