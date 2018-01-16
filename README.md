## Welcome to SCENE

SCENE - Smart Contract Events listeNEr

## TODO:

- [x] write real README
- [ ] integration tests
- [ ] spec all pure function
- [x] configuration via env variable
- [ ] add comand line options to configure app
- [ ] doc


### Prequisites

[Docker](https://docs.docker.com/engine/installation/) and [docker-compose](https://docs.docker.com/compose/install/) needs to be installed to run the application.


### running in development mode

run the following command in the terminal to start Figwheel:

```
docker-compose up repl
```


#### configuring the REPL

Once Figwheel and node are running, you can connect to the remote REPL at `localhost:7000`.

Type following code in the REPL to connect to Figwheel ClojureScript REPL.

```clojure
(do (start-fw) 
    (cljs))
```


### building the release version

```
docker-compose up release
```

