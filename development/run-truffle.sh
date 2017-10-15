#!/usr/bin/env sh

truffle compile
truffle deploy --reset
truffle exec fireEvents.js
