#!/usr/bin/env sh

BOOTNODES=$(tr '\n' ',' < /bootnodes)
geth --http --http.addr 0.0.0.0 --ropsten --syncmode "light" --bootnodes "${BOOTNODES}"
