FROM alpine:3.13
ARG version=8.342.07.4

ENV PATH=/usr/bin/java:$PATH

RUN apk update && apk add bash && apk add shadow && apk add curl && apk add curl && apk add openjdk11 && apk add busybox-extras

RUN groupadd kafka_group
RUN useradd -s /bin/bash -m -c "KAFKA" -Gkafka_group kafka

USER kafka
WORKDIR /home/kafka

RUN mkdir -p /home/kafka/downloads \
&& curl https://downloads.apache.org/kafka/3.2.1/kafka_2.12-3.2.1.tgz --output /home/kafka/downloads/kafka.tgz \
&& mkdir /home/kafka/kafka \
&& cd /home/kafka/kafka \
&& tar -xvzf /home/kafka/downloads/kafka.tgz --strip 1

RUN mkdir -p /home/kafka/scripts/
COPY ./scripts/start.sh /home/kafka/scripts/

#ENTRYPOINT [ "/bin/bash",  "/home/gitpod/scripts/start.sh" ]