FROM nimmis/apache-php5

# COPY default.conf /etc/apache2/sites-available/default.conf
COPY 000-default.conf /etc/apache2/sites-available/000-default.conf
# COPY pga-default.conf /etc/apache2/sites-available/pga-default.conf
RUN mkdir /var/www/portals
RUN a2enmod rewrite
EXPOSE 80
EXPOSE 443
CMD [ "/bin/sh", "-c", "mkdir -p /var/www/portals/default/app/storage/views/ && mkdir -p /var/www/portals/default/app/storage/sessions/ && composer --working-dir=/var/www/portals/default install && echo 'Starting sever..' && /usr/sbin/apache2ctl -D FOREGROUND"]