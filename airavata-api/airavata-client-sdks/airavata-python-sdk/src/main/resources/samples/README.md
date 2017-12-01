# AppCatalogCLI

Steps to run the code

To export all modules, interface and deployments in JSON format

Generate accesstoken using following command -
curl --data "username=USERNAME&password=PASSWORD" https://dev.apptestdrive.airavata.org/api-login

URL procided above shouldbe the URL of gateway you want to export Application Catalog

Give GatewayId and AccessToken in airavata.ini

Run export_application_catalog.
 It take one parameter USERNAME.

python3  export_application_catalog.py getdeploy USERNAME

Three files will generate - DeploysData.txt, InterfaceData.txt and NodulesData.txt.

To import all modules, interface and deployments

Generate accesstoken using following command -
curl --data "username=USERNAME&password=PASSWORD" https://dev.apptestdrive.airavata.org/api-login

URL procided above shouldbe the URL of gateway you want to import Application Catalog

Give Username, GatewayId and AccessToken in airavata.ini

Run import_application_catalog.py

python3 importapplication_catalog.py