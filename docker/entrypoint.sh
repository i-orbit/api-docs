#!/bin/bash
java -Djava.security.egd=file:/dev/./urandom \
            ${JVM_OPTS} \
            -jar /home/app.jar \
            --spring.profiles.active=${PROFILE:-prod} \
