#!/bin/bash
set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <portal_name> <domain_name>"
    exit 1
fi

PORTAL_NAME=$1
DOMAIN_NAME=$2

PORTALS_BASE_DIR="/var/www/portals"
APACHE_USER="pga"
APACHE_GROUP="pga"
PYTHON_EXECUTABLE="python3.9"

PROJECT_ROOT="${PORTALS_BASE_DIR}/django-${PORTAL_NAME}"
VENV_PATH="${PROJECT_ROOT}/venv"
SETTINGS_LOCAL_SRC="./settings_local_${PORTAL_NAME}.py"

# Assumes python3.9 and certbot are already installed
echo ">>> Installing essential build tools..."
sudo apt-get update
sudo apt-get install -y python3-pip git apache2 gcc apache2-dev libmysqlclient-dev npm

echo ">>> Setting up project directory..."
if [ ! -d "${PROJECT_ROOT}" ]; then
    sudo mkdir -p ${PROJECT_ROOT}
fi
TMP_CLONE_DIR=$(mktemp -d)
git clone https://github.com/apache/airavata-django-portal.git ${TMP_CLONE_DIR}
sudo rsync -av --delete ${TMP_CLONE_DIR}/ ${PROJECT_ROOT}/airavata-django-portal/
rm -rf ${TMP_CLONE_DIR}

echo ">>> Creating Python 3.9 virtual environment..."
if [ ! -d "${VENV_PATH}" ]; then
    sudo ${PYTHON_EXECUTABLE} -m venv ${VENV_PATH}
fi

echo ">>> Installing Python dependencies..."
sudo ${VENV_PATH}/bin/pip install --upgrade pip setuptools wheel
sudo bash -c "cd ${PROJECT_ROOT}/airavata-django-portal && ${VENV_PATH}/bin/pip install -r requirements.txt"
sudo ${VENV_PATH}/bin/pip install mod_wsgi mysqlclient==2.2.0

echo ">>> Building frontend assets..."
cd ${PROJECT_ROOT}/airavata-django-portal
sudo bash -c 'export NVM_DIR="/root/.nvm"; [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" ; nvm install 19 && npm install -g yarn && ./build_js.sh'
cd -

echo ">>> Configuring Django..."
if [ ! -f "$SETTINGS_LOCAL_SRC" ]; then
    echo "ERROR: Local settings file ${SETTINGS_LOCAL_SRC} not found!"
    exit 1
fi
sudo cp ${SETTINGS_LOCAL_SRC} ${PROJECT_ROOT}/airavata-django-portal/django_airavata/settings_local.py
sudo ${VENV_PATH}/bin/python ${PROJECT_ROOT}/airavata-django-portal/manage.py migrate --noinput
sudo ${VENV_PATH}/bin/python ${PROJECT_ROOT}/airavata-django-portal/manage.py collectstatic --noinput

echo ">>> Configuring Apache..."
VHOST_CONF="/etc/apache2/sites-available/${PORTAL_NAME}.conf"

sudo bash -c "cat > ${VHOST_CONF}" <<EOF
<VirtualHost *:80>
    ServerName ${DOMAIN_NAME}

    ## Redirect all http traffic to https
    RewriteEngine On
    RewriteCond %{HTTPS} off
    RewriteRule (.*) https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]
</VirtualHost>

<VirtualHost *:443>
    ServerName ${DOMAIN_NAME}
    TimeOut 300

    # Redirect root to /admin
    RedirectMatch ^/$ /admin

    Alias /robots.txt ${PROJECT_ROOT}/static/robots.txt
    Alias /favicon.ico ${PROJECT_ROOT}/static/favicon.ico

    Alias /static/ ${PROJECT_ROOT}/static/
    <Directory ${PROJECT_ROOT}/static>
        Require all granted
        AddOutputFilterByType DEFLATE text/html text/plain text/xml text/css text/javascript application/javascript image/svg+xml
        Header set Cache-Control "no-cache"
        RequestHeader edit "If-None-Match" "^\"((.*)-gzip)\"$" "\"\\\$1\", \"\\\$2\""
        <FilesMatch "\.[0-9a-f]{8}\.(css|js|svg)$">
            Header set Cache-Control "max-age=31536000, public"
        </FilesMatch>
    </Directory>

    Alias /media/ ${PROJECT_ROOT}/airavata-django-portal/django_airavata/media/
    <Directory ${PROJECT_ROOT}/airavata-django-portal/django_airavata/media>
        Require all granted
    </Directory>

    WSGIDaemonProcess ${DOMAIN_NAME} \\
        display-name=%{GROUP} \\
        python-home=${VENV_PATH} \\
        python-path=${PROJECT_ROOT}/airavata-django-portal:${VENV_PATH}/lib/python3.9/site-packages \\
        processes=1 \\
        user=${APACHE_USER} \\
        group=${APACHE_GROUP} \\
        lang=en_US.UTF-8 \\
        locale=en_US.UTF-8
    WSGIProcessGroup ${DOMAIN_NAME}

    WSGIScriptAlias / ${PROJECT_ROOT}/airavata-django-portal/django_airavata/wsgi.py
    WSGIPassAuthorization On

    <Directory ${PROJECT_ROOT}/airavata-django-portal/django_airavata>
        <Files wsgi.py>
            Require all granted
        </Files>
    </Directory>

    ErrorLog /var/log/apache2/${PORTAL_NAME}.error.log
    CustomLog /var/log/apache2/${PORTAL_NAME}.requests.log combined

    SSLEngine on
    SSLCertificateFile /etc/letsencrypt/live/${DOMAIN_NAME}/cert.pem
    SSLCertificateChainFile /etc/letsencrypt/live/${DOMAIN_NAME}/fullchain.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/${DOMAIN_NAME}/privkey.pem
</VirtualHost>
EOF

echo ">>> Setting file permissions..."
sudo chown -R ${APACHE_USER}:${APACHE_GROUP} ${PROJECT_ROOT}
sudo chmod -R 775 ${PROJECT_ROOT}

echo ">>> Enabling Site, running Certbot, and restarting..."
sudo a2dissite 000-default.conf || true
sudo a2enmod ssl rewrite headers
sudo a2ensite ${PORTAL_NAME}.conf
sudo systemctl restart apache2

sudo certbot --apache -d ${DOMAIN_NAME} --non-interactive --agree-tos -m ARTISAN@groups.gatech.edu --redirect

sudo systemctl restart apache2

echo ">>> Deployment Complete."