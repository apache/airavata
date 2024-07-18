from setuptools import setup, find_packages

setup(
    name='airavata_jupyter_magic',
    version='0.5',
    packages=find_packages(),
    install_requires=[
        # Add any dependencies your extension needs here
    ],
    include_package_data=True,
    description='A custom IPython magic extension for Airavata to recute local notebooks on clusters',
    long_description=open('README.md').read(),
    long_description_content_type='text/markdown',
    author='Dimuthu Wannipurage',
    author_email='dimuthuw@gatech.edu',
    url='https://github.com/apache/airavata',  # Update with your repo link
    classifiers=[
        'Programming Language :: Python :: 3',
        'License :: OSI Approved :: Apache Software License',
        'Operating System :: OS Independent',
    ],
    license='Apache License 2.0',
)