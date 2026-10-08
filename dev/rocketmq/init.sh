#!/bin/sh
set -eu
for topic in LanewayBusiness LanewayOrderDeadline; do
  sh mqadmin updateTopic -n mq-nameserver:9876 -b rocketmq:10911 -t "$topic" -r 4 -w 4
done
