FROM node:9.2.0

MAINTAINER Mateusz Probachta <mateusz.probachta@gmail.com>

COPY ./release /scene
WORKDIR /scene

RUN yarn install
ENV HOST 0.0.0.0

EXPOSE 3000

CMD yarn start
