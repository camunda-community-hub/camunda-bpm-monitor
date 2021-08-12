#!/bin/sh
mvn package -Dmaven.test.skip=true
docker build --pull --rm -f Dockerfile -t camundamonitoring .
docker stack deploy process --compose-file docker-compose.yml