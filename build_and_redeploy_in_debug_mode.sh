#!/bin/sh

docker build -t super-home -f Dockerfile_debug .
echo Stopping...
docker stop super-home-container || true
echo OK
echo Removing...
docker rm super-home-container || true
echo OK
echo Starting...
docker run --network=super_home_nw --rm -it --publish 8080:8080 --publish 8000:8000 --mount type=volume,source=super_home_data_volume,destination=/data --name super-home-container super-home
echo OK