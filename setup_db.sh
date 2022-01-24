#! /bin/sh

# Setup restheart access control

curl --user admin:secret -X PUT localhost:8080/restheart
curl --user admin:secret -X PUT localhost:8080/restheart/users
curl --user admin:secret -X PUT localhost:8080/restheart/acl
curl --user admin:secret -H "Content-Type:application/json" localhost:8080/restheart/acl -d @public_acl.json

# Setup oida db

curl --user admin:secret -X PUT localhost:8080/oida
curl --user admin:secret -X PUT localhost:8080/oida/doc

# Create text index.
curl --user admin:secret -X PUT localhost:8080/oida/doc/_indexes/text -H "Content-Type:application/json" -d '{ "keys": { "pages.text": "text" } }'

# Load in data

# curl --user admin:secret -H "Content-Type:application/json" localhost:8080/oida/doc -d @data.json
