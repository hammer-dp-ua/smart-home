#!/bin/sh

docker build -t super-home . &&
docker stop super-home-container || true
docker rm super-home-container || true
docker run --network=super_home_nw --restart always --detach --publish 8080:8080 --mount type=volume,source=super_home_data_volume,destination=/data --name super-home-container super-home
