#!/usr/bin/env bash

exec /usr/sbin/init

if systemctl |grep running | grep -Fq 'iptables'; then
    systemctl stop iptables
    systemctl mask iptables
fi

systemctl start firewalld

firewall-cmd --zone=public --permanent --add-service=http

firewall-cmd --zone=public --permanent --add-service=https
