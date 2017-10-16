## Welcome to SCENE

SCENE - Smart Contract Events listeNEr

## TODO:

- [] write real README

### Prequisites

[Node.js](https://nodejs.org/en/) needs to be installed to run the application.

### running in development mode

run the following command in the terminal to install NPM modules and start Figwheel:

```
lein build
```

run `node` in another terminal:

```
npm start
```

#### configuring the REPL

Once Figwheel and node are running, you can connect to the remote REPL at `localhost:7000`.

Type `(cljs)` in the REPL to connect to Figwheel ClojureScript REPL.


### building the release version

```
lein package
```

Run the release version:

```
npm start
```

### Running using Docker

The template comes with a `Dockerfile` for running the application using Docker

Once you've run `lein package`, you can build and run a Docker container as follows:

```
docker build -t scene:latest .
docker run -p 3000:3000 scene:latest
```