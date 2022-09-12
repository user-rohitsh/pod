FROM gitpod/workspace-full:2022-05-08-14-31-53
RUN mkdir -p ~/downloads \
&& curl https://downloads.apache.org/kafka/3.2.1/kafka_2.12-3.2.1.tgz --output ~/downloads/kafka.tgz \
&& mkdir ~/kafka \
&& cd ~/kafka \
&& tar -xvzf ~/downloads/kafka.tgz --strip 1

RUN sudo apt install telnet
