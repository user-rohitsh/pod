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