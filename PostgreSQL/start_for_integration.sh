#!/bin/sh

docker run --rm -it --name postgres-tests --publish 5433:5432 --tmpfs /var/lib/postgresql/data:rw -v "/home/servak/PostgreSQL/tests-postgres.conf":/etc/postgresql/postgresql.conf postgres-image-tests -c config_file=/etc/postgresql/postgresql.conf
