#!/bin/bash

nohup ~/kafka/bin/zookeeper-server-start.sh ~/kafka/config/zookeeper.properties >~/start_zoo.log 2>&1 &
sleep 20
~/kafka/bin/kafka-server-start.sh ~/kafka/config/server.properties >~/start_kafka.log 2>&1
