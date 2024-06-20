# AppCatalogCLI

Steps to run the code

To export all modules, interface and deployments in JSON format

Generate Accesstoken using following command -
curl --data "username=USERNAME&password=PASSWORD" https://dev.apptestdrive.airavata.org/api-login

URL provided above should be the URL of gateway from which you want to export Application Catalog

Give GatewayId and AccessToken in airavata.ini file

Run export_application_catalog.py . It takes one parameter USERNAME.

python3 export_application_catalog.py getdeploy USERNAME

Three files will generate - DeploysData.txt, InterfaceData.txt and NodulesData.txt. These file contain all the required data to import in JSON format.

To import all modules, interface and deployments

Generate Accesstoken using following command -
curl --data "username=USERNAME&password=PASSWORD" https://dev.apptestdrive.airavata.org/api-login

URL provided above should be the URL of gateway to which you want to import Application Catalog

Give Username, GatewayId and AccessToken in airavata.ini

Run import_application_catalog.py

python3 import_application_catalog.py
