#This Dockerfile should be considered as the Base docker image for all the services
FROM centos:7

RUN yum -y update; yum clean all

RUN yum install firewalld -y && \
    yum install git -y && \
    yum install httpd -y && \
    yum install mod_ssl -y && \
    yum install php -y && \
    yum install php-soap -y && \
    yum install libselinux-python -y && \
    yum install policycoreutils-python -y && \
    yum install php-pdo -y && \
    yum install epel-release -y && \
    yum install php-mcrypt -y

RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer


RUN  mkdir -p /var/www/portals/gateway_id

RUN  mkdir -p /var/www/portals/expr_data

RUN  chown -R apache:apache /var/www/portals

RUN  chmod 0777 /var/www/portals/expr_data

RUN  chmod a+rX /var/www/portals/gateway_id

WORKDIR /var/www/portals/gateway_id

RUN git clone https://github.com/apache/airavata-php-gateway.git .

USER root

#RUN  chown apache:apache /var/www/portals/gateway_id/app/config/pga_config.php

RUN  chmod 0777  /var/www/portals/gateway_id/app/storage

RUN  chown -R apache:apache /var/www/portals

RUN /usr/local/bin/composer update

RUN apachectl -k graceful

ADD scripts/run.sh /run.sh

RUN chmod -v +x /run.sh

EXPOSE 80

CMD ["/run.sh"]





