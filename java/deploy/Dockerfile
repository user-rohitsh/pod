FROM alpine:3.14

RUN  apk update \
  && apk upgrade \
  && apk add ca-certificates \
  && apk add --update coreutils  \
  && rm -rf /var/cache/apk/* \
  && apk add --update shadow openjdk11 sudo tzdata curl unzip bash busybox-extras \
  && rm -rf /var/cache/apk/*

RUN addgroup -S docker
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home /home/docker \
    --ingroup docker \
    docker

USER docker
WORKDIR /home/docker

COPY deploy/opentelemetry-javaagent.jar ./

COPY "TestService/target/TestService-0.0.1-SNAPSHOT.jar" ./
COPY "TestService1/target/TestService1-0.0.1-SNAPSHOT.jar" ./