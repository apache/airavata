FROM php:8.1-apache

# Install required PHP extensions and tools from apt
RUN apt-get update && apt-get install -y wget git unzip && rm -rf /var/lib/apt/lists/*

# Install Composer (official way)
RUN wget https://getcomposer.org/installer -O composer-setup.php && \
  php composer-setup.php --install-dir=/usr/local/bin --filename=composer && \
  rm composer-setup.php

# Set up your application (example)
RUN mkdir -p /var/www/portals/default

RUN wget -q https://github.com/apache/airavata-portals/archive/refs/heads/main.tar.gz -O /tmp/portals.tar.gz && \
  tar -xzf /tmp/portals.tar.gz --strip-components=2 -C /var/www/portals/default airavata-portals-main/airavata-php-gateway && \
  rm /tmp/portals.tar.gz

COPY pga-default.conf /etc/apache2/sites-available/000-default.conf

# Enable Apache modules
RUN a2enmod rewrite

RUN mkdir -p /var/www/portals/default/storage/views && \
    mkdir -p /var/www/portals/default/storage/sessions && \
    mkdir -p /var/www/portals/default/bootstrap/cache

# Set proper permissions for Laravel
RUN chmod -R 775 /var/www/portals/default/storage/views && \
    chmod -R 775 /var/www/portals/default/storage/sessions && \
    chmod -R 775 /var/www/portals/default/bootstrap/cache

WORKDIR /var/www/portals/default
RUN composer install

EXPOSE 80
EXPOSE 443
