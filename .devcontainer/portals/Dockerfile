FROM php:8.1-apache

# TODO put all 3 portals --  research portal (:5173), php gateway (:8008), and django portal (:8009)
# TODO handle ssl termination before requests hit this container

ARG PORTALS_REPO_URL=https://github.com/apache/airavata-portals/archive/refs/heads/main.tar.gz

# set hostname
RUN echo "ServerName airavata.host" >> /etc/apache2/apache2.conf

# install deps
RUN apt-get update && apt-get install -y wget git unzip && rm -rf /var/lib/apt/lists/*
RUN wget -qO- https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer

# setup site config
COPY pga-default.conf /etc/apache2/sites-available/000-default.conf

RUN a2enmod rewrite && a2ensite 000-default.conf && apache2ctl configtest

USER www-data:www-data

WORKDIR /var/www/html/default

# ensure config directory exists for mounting
RUN mkdir -p config storage/views storage/sessions bootstrap/cache

RUN wget -qO- $PORTALS_REPO_URL | tar xz --strip-components=2 -C . airavata-portals-main/airavata-php-gateway

COPY pga_config.php /var/www/html/default/config/pga_config.php

RUN composer install --no-dev --optimize-autoloader

EXPOSE 5173 8008 8009
