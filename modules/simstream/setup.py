"""
    Setup for simstream module.

    Author: Jeff Kinnison (jkinniso@nd.edu)
"""

from setuptools import setup, find_packages

setup(
    name="simstream",
    version="0.1dev",
    author="Jeff Kinnison",
    author_email="jkinniso@nd.edu",
    packages=find_packages(),
    description="",
    install_requires=[
        "pika >= 0.10.0"
    ],
)
