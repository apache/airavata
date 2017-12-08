#!/bin/sh

# Create Default RabbitMQ setup
( sleep 10 ; \ 

rabbitmqctl add_user airavata airavata; \

rabbitmqctl set_user_tags airavata administrator ; \

rabbitmqctl add_vhost messaging ; \

rabbitmqctl set_permissions -p messaging airavata "." "." ".*" ; \

rabbitmqctl set_user_tags airavata administrator;\

) &    
rabbitmq-server $@

