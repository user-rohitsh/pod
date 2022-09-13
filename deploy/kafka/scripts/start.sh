#!/bin/bash
nohup ~/kafka_scripts/start_zookeeper.sh &
sleep 2
nohup ~/kafka_scripts/start_kafka.sh &
touch ~/1
sudo chmod 777 ~/1
tail -f ~/1