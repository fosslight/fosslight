# Copyright (c) Sameer1046
# SPDX-License-Identifier: AGPL-3.0-only

# Build the image from source
FROM gradle:8.5-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle build --no-daemon --exclude-task test

ENV JAVA_OPTS="-Xmx8g -XX:MaxRamPercentage=80"

# Create the containerized app
FROM eclipse-temurin:11.0.21_9-jre-jammy
LABEL maintainer="FOSSLight <fosslight-dev@lge.com>"

COPY --from=build /home/gradle/src/build/libs/*.war /app/FOSSLight.war
COPY ./verify/verify /app/verify/verify
COPY ./db/wait-for /app/wait-for
COPY ./LICENSES /app/LICENSES

ADD ./src/main/resources/template /app/template

RUN chmod +x /app/wait-for /app/verify/verify && \
    apt-get update && \
    apt-get install -y --no-install-recommends netcat && \
    rm -rf /var/lib/apt/lists/* && \
    ln -s /bin/sh bash

WORKDIR /app
CMD ["java" , "-jar", "FOSSLight.war", "--root.dir=/data/fosslight", "--server.port=8180"]
