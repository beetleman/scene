#!/usr/bin/env bash
source /usr/local/nvm/nvm.sh


function scene {

    TARGET=target/out/scene.js
    rm -rf $TARGET
    while `sleep 1`;
    do
        if [ -f $TARGET ]; then
            node --inspect=0.0.0.0:9229  $TARGET
        fi
    done;
}

scene &

case $1 in
    app)
        echo "starting build..."
        lein build
        ;;
    repl)
        echo "starting repl..."
        lein repl-dev
        ;;
esac
