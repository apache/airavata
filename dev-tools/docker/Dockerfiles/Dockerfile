#This Dockerfile should be considered as the Base docker image for all the services
FROM centos:7
RUN useradd -ms /bin/bash pga

RUN yum -y update; yum clean all
RUN yum -y install systemd; yum clean all; \
(cd /lib/systemd/system/sysinit.target.wants/; for i in *; do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; done); \
rm -f /lib/systemd/system/multi-user.target.wants/*;\
rm -f /etc/systemd/system/*.wants/*;\
rm -f /lib/systemd/system/local-fs.target.wants/*; \
rm -f /lib/systemd/system/sockets.target.wants/*udev*; \
rm -f /lib/systemd/system/sockets.target.wants/*initctl*; \
rm -f /lib/systemd/system/basic.target.wants/*;\
rm -f /lib/systemd/system/anaconda.target.wants/*;

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


ADD scripts/firewalld_config.sh /tmp/firewalld_config.sh

RUN chmod +x /tmp/firewalld_config.sh

RUN  mkdir -p /var/www/portals/gateway_id

RUN  mkdir -p /var/www/expr_data

RUN  chown -R pga:pga /var/www/portals/gateway_id

RUN  chmod 0777 /var/www/expr_data

RUN  chmod a+rX /var/www/portals/gateway_id

USER pga

WORKDIR /var/www/portals/gateway_id

RUN git clone https://github.com/apache/airavata-php-gateway.git .

ADD pga_config.php.j2 /tmp/

ADD pga-vhost.conf.j2 /tmp/

ADD default.conf /tmp/

USER root

RUN  mv /tmp/default.conf /etc/httpd/conf.d/default.conf

RUN  mv /tmp/pga_config.php.j2 /var/www/portals/gateway_id/app/config/pga_config.php

RUN  mv /tmp/pga-vhost.conf.j2 /etc/httpd/conf.d/pga-gateway_id.conf

RUN  chown pga:pga /var/www/portals/gateway_id/app/config/pga_config.php

RUN  chmod 0777  /var/www/portals/gateway_id/app/storage

RUN /usr/local/bin/composer update

EXPOSE 80

VOLUME ["/sys/fs/cgroup"]

#RUN bash /tmp/firewalld_config.sh

CMD ["bash","/tmp/firewalld_config.sh"]





