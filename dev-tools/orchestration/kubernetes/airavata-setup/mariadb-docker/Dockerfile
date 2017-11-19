from mariadb:10.2
#####ADD sqlstartup.sql /docker-entrypoint-initdb.d/sqlstartup.sql
ENV MYSQL_USER=airavata
ENV MYSQL_PASSWORD=airavata
ENV MYSQL_ROOT_PASSWORD=root123
ADD sqlstartup.sql /docker-entrypoint-initdb.d/sqlstartup.sql
expose 3306
