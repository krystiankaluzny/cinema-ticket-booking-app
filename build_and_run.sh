#!/bin/bash

if [[ -z $JAVA_HOME ]]; then
  echo "Missing JAVA_HOME env variables"
  exit 1
fi


mvn clean package
"$JAVA_HOME"/bin/java -jar target/cinema-ticket-booking-app-1.0-SNAPSHOT.jar