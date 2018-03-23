#!/bin/bash

NODE_VERSION=v9.2.0

apt-get update
apt-get install build-essential -y
apt-get install curl -y
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.5/install.sh | bash

source ~/.bashrc
nvm install $NODE_VERSION
nvm alias default $NODE_VERSION
nvm use default
