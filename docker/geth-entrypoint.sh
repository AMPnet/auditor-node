#!/usr/bin/env sh

BOOTNODES=$(tr '\n' ',' < /bootnodes)
geth --http --http.addr 0.0.0.0 --goerli --syncmode "light" --bootnodes "${BOOTNODES}"
