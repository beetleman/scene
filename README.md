# Welcome to SCENE

SCENE - Smart Contract Events listeNEr

## Prequisites

[Docker](https://docs.docker.com/engine/installation/) and [docker-compose](https://docs.docker.com/compose/install/) needs to be installed to run the application.

## Usage

### Run inside docker

Copy file from `example` directory or copy&pase this code to `docker-compose.yml`:

```yaml
version: "2.2"

services:
  scene:
    image: beetleman/scene
    ports:
      - "3000:3000"
    links:
      - mongo
      - kovan
    environment:
      - RPC_URL=http://kovan:8545
      - MONGO_URL=mongodb://mongo:27017/dev
      - DB_NAME=scene

  mongo:
    image: mongo:3.6.0

  kovan:
    image: parity/parity:v1.9.5
    cpus: 0.5
    volumes:
      - kovan-chain:/root/.local/share/io.parity.ethereum
    command: --chain kovan --unsafe-expose --no-warp --tracing on

volumes:
  kovan-chain:
```

Run in

``` shell
docker-compose upp scene
```
app will be avialable on http://localhost:3000

### How to use REST API

- get last 1000 events for `Transfer` from [ERC20](https://theethereum.wiki/w/index.php/ERC20_Token_Standard)

``` shell
curl --request POST \
  --url http://localhost:3000/events \
  --header 'content-type: application/json' \
  --data '
      {
      "anonymous": false,
      "inputs": [
        {
          "indexed": true,
          "name": "_from",
          "type": "address"
        },
        {
          "indexed": true,
          "name": "_to",
          "type": "address"
        },
        {
          "indexed": false,
          "name": "_value",
          "type": "uint256"
        }
      ],
      "name": "Transfer",
      "type": "event"
    }
'
```

- get last 1000 events for `Transfer` from `Dai Stablecoin v1.0` using query parametr

``` shell
curl --request POST \
  --url http://localhost:3000/events?address=0xc4375b7de8af5a38a93548eb8453a498222c4ff2 \
  --header 'content-type: application/json' \
  --data '
      {
      "anonymous": false,
      "inputs": [
        {
          "indexed": true,
          "name": "_from",
          "type": "address"
        },
        {
          "indexed": true,
          "name": "_to",
          "type": "address"
        },
        {
          "indexed": false,
          "name": "_value",
          "type": "uint256"
        }
      ],
      "name": "Transfer",
      "type": "event"
    }
'
```

### How to use WS

After connect this types of mesages will be avialable

- example of subscribe call, address field is optional

```json
{
  "type": "subscribe",
  "payload": {
    "address": "0x0",
    "abi": {
      "anonymous": false,
      "inputs": [
        {
          "indexed": true,
          "name": "_owner",
          "type": "address"
        },
        {
          "indexed": false,
          "name": "_value",
          "type": "uint256"
        }
      ],
      "name": "Balance",
      "type": "event"
    }
  }
}
```

- example of unsubscribe call

```json
{
  "type": "unsubscribe",
  "payload": {
    "address": "0x0",
    "abi": {
      "anonymous": false,
      "inputs": [
        {
          "indexed": true,
          "name": "_owner",
          "type": "address"
        },
        {
          "indexed": false,
          "name": "_value",
          "type": "uint256"
        }
      ],
      "name": "Balance",
      "type": "event"
    }
  }
}
```

## Devalopment

### running in development mode

run the following command in the terminal to start Figwheel:

```shell
docker-compose up repl
```

#### configuring the REPL

Once Figwheel and node are running, you can connect to the remote REPL at `localhost:7000`.

Type following code in the REPL to connect to Figwheel ClojureScript REPL.

```clojure
(quick-dev)
```

### building the release version

```
docker-compose up release
```

## TODO:

* [ ] write real README
* [ ] integration tests
* [ ] spec all pure function
* [x] configuration via env variable
* [ ] add more filter options
    * [ ] `from`
    * [ ] `to`
    * [x] `address`
* [ ] doc
