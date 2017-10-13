FROM mhart/alpine-node:latest

MAINTAINER Mateusz Probachta <mateusz.probachta@gmail.com>

# Create app directory
RUN mkdir -p /scene
WORKDIR /scene

# Install app dependencies
COPY package.json /scene
RUN npm install pm2 -g
RUN npm install

# Bundle app source
COPY target/release/scene.js /scene/scene.js
COPY public /scene/public

ENV HOST 0.0.0.0

EXPOSE 3000
CMD [ "pm2-docker", "/scene/scene.js" ]
