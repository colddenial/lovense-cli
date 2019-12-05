#!/bin/bash
if [ "$1" != "" ];
then
    echo Running Test: $1
else
    echo compilation test only!!!
fi
mvn clean package
if [ "$1" != "" ];
then
    java -jar target/lovense-cli-1.0-SNAPSHOT.jar $1
fi
