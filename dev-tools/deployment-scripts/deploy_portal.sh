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
PYTHON_EXECUTABLE="python3.10"

PROJECT_ROOT="${PORTALS_BASE_DIR}/django-${PORTAL_NAME}"
VENV_PATH="${PROJECT_ROOT}/venv"
SETTINGS_LOCAL_SRC="./settings_local_${PORTAL_NAME}.py"

echo ">>> Installing Python 3.10 from source..."
sudo apt update
sudo apt install -y build-essential zlib1g-dev libncurses5-dev libgdbm-dev libnss3-dev libssl-dev libreadline-dev libffi-dev libsqlite3-dev wget libbz2-dev

PYTHON_TAR="Python-3.10.13.tgz"
PYTHON_DIR="Python-3.10.13"
PYTHON_URL="https://www.python.org/ftp/python/3.10.13/$PYTHON_TAR"

if ! python3.10 --version &> /dev/null; then
    TEMP_DIR=$(mktemp -d)
    pushd ${TEMP_DIR}
    wget "$PYTHON_URL"
    tar -xf "$PYTHON_TAR"
    cd "$PYTHON_DIR"
    ./configure --enable-optimizations --enable-shared
    sudo make altinstall
    sudo ldconfig
    popd
    rm -rf ${TEMP_DIR}
else
    echo "Python 3.10 already detected."
fi

# Remove system mod_wsgi package before compiling the custom one
echo ">>> Removing system mod_wsgi package..."
sudo a2dismod wsgi || true
sudo apt remove --purge -y libapache2-mod-wsgi-py3 || true

echo ">>> Installing essential build tools (including apache2-dev for mod_wsgi compilation)..."
sudo apt-get install -y python3-pip git apache2 gcc apache2-dev libmysqlclient-dev npm certbot python3-certbot-apache pkg-config default-libmysqlclient-dev

# Compile mod_wsgi against Python 3.10
echo ">>> Compiling mod_wsgi against Python 3.10..."
MOD_WSGI_VERSION="5.0.2"
MOD_WSGI_TAR="mod_wsgi-${MOD_WSGI_VERSION}.tar.gz"
MOD_WSGI_URL="https://github.com/GrahamDumpleton/mod_wsgi/archive/refs/tags/${MOD_WSGI_VERSION}.tar.gz"

TEMP_MOD_WSGI_DIR=$(mktemp -d)
pushd "${TEMP_MOD_WSGI_DIR}"
wget -O "${MOD_WSGI_TAR}" "$MOD_WSGI_URL"
tar -xf "${MOD_WSGI_TAR}"
cd "mod_wsgi-${MOD_WSGI_VERSION}"

./configure --with-apxs=/usr/bin/apxs --with-python="${VENV_PATH}/bin/python3.10"
sudo make
sudo make install

echo ">>> Creating wsgi.load for Apache..."
sudo bash -c 'cat > /etc/apache2/mods-available/wsgi.load' <<'EOL_WSGI_LOAD'
LoadModule wsgi_module /usr/lib/apache2/modules/mod_wsgi.so
EOL_WSGI_LOAD

popd
rm -rf "${TEMP_MOD_WSGI_DIR}"

echo ">>> Enabling custom mod_wsgi module..."
sudo a2enmod wsgi

echo ">>> Setting up project directory..."
if [ ! -d "${PROJECT_ROOT}" ]; then
    sudo mkdir -p ${PROJECT_ROOT}
fi
TMP_CLONE_DIR=$(mktemp -d)
git clone https://github.com/apache/airavata-django-portal.git ${TMP_CLONE_DIR}
sudo rsync -av --delete ${TMP_CLONE_DIR}/ ${PROJECT_ROOT}/airavata-django-portal/
rm -rf ${TMP_CLONE_DIR}

echo ">>> Creating Python 3.10 virtual environment..."
if [ ! -d "${VENV_PATH}" ]; then
    sudo ${PYTHON_EXECUTABLE} -m venv ${VENV_PATH}
fi

echo ">>> Installing Python dependencies..."
sudo ${VENV_PATH}/bin/pip install --upgrade pip setuptools wheel
sudo bash -c "cd ${PROJECT_ROOT}/airavata-django-portal && ${VENV_PATH}/bin/pip install -r requirements.txt"
sudo ${VENV_PATH}/bin/pip install mod_wsgi mysqlclient==2.2.0

echo ">>> Building frontend assets..."
cd ${PROJECT_ROOT}/airavata-django-portal
sudo npm install -g yarn
sudo bash -c "cd ${PROJECT_ROOT}/airavata-django-portal && ./build_js.sh"
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
        python-path=${PROJECT_ROOT}/airavata-django-portal:${VENV_PATH}/lib/python3.10/site-packages \\
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

    #SSLEngine on
    #SSLCertificateFile /etc/letsencrypt/live/${DOMAIN_NAME}/cert.pem
    #SSLCertificateChainFile /etc/letsencrypt/live/${DOMAIN_NAME}/fullchain.pem
    #SSLCertificateKeyFile /etc/letsencrypt/live/${DOMAIN_NAME}/privkey.pem
</VirtualHost>
EOF

echo ">>> Setting file permissions..."
sudo chown -R ${APACHE_USER}:${APACHE_GROUP} ${PROJECT_ROOT}
sudo chmod -R 775 ${PROJECT_ROOT}

echo ">>> Enabling Site, running Certbot, and restarting..."
sudo rm /etc/apache2/sites-enabled/default.conf || true
sudo a2dissite 000-default.conf || true
sudo a2enmod ssl rewrite headers wsgi
sudo a2ensite ${PORTAL_NAME}.conf

sudo certbot --apache -d ${DOMAIN_NAME} --non-interactive --agree-tos -m ARTISAN@groups.gatech.edu --redirect

sudo systemctl restart apache2

echo ">>> Deployment Complete."