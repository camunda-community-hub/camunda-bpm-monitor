#!/bin/sh
mvn package -Dmaven.test.skip=true
docker build --pull --rm -f Dockerfile -t camundamonitoring .
