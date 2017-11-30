FROM node:9.2.0-alpine

MAINTAINER Mateusz Probachta <mateusz.probachta@gmail.com>

RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh

COPY ./release /scene
WORKDIR /scene

RUN yarn install
ENV HOST 0.0.0.0

EXPOSE 3000

CMD yarn start
