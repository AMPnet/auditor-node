#!/bin/sh

# IPFS apparently does not like having any whitespaces in datastore_spec file, so we remove them here
tr -d " \t\n\r" < /data/ipfs/raw_datastore_spec > /data/ipfs/datastore_spec

/sbin/tini -s -- /usr/local/bin/start_ipfs daemon --migrate=true
