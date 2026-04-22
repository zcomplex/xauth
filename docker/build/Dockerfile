FROM alpine:latest

ARG ENVIRONMENT
ENV ENVIRONMENT=$ENVIRONMENT

ARG APP_SECRET
ENV APP_SECRET=$APP_SECRET

ARG APP_VERSION
ENV APP_VERSION=$APP_VERSION

ARG APP_NAME
ENV APP_NAME=$APP_NAME \
    SCALA_VERSION="2.12.6" \
    SCALA_HOME=/usr/share/scala

ARG APP_ARCHIVE="$APP_NAME-$APP_VERSION.zip"

RUN apk update; \
    apk upgrade; \
    apk add openjdk8; \
    apk add nss; \
    apk add --virtual=.build-dependencies wget ca-certificates; \
    apk add bash; \
    cd "/tmp"; \
    wget "http://downloads.lightbend.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz"; \
    tar xzf "scala-${SCALA_VERSION}.tgz"; \
    mkdir "${SCALA_HOME}"; \
    rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat; \
    mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}"; \
    ln -s "${SCALA_HOME}/bin/"* "/usr/bin/"; \
    apk del .build-dependencies; \
    rm -rf "/tmp/"* \
    rm -fr /var/cache/apk/*

# Installing required packages to import rds-combined-ca-bundle.pem into keystore
RUN apk add coreutils openssl perl

# Cleaning
#RUN apk del curl coreutils openssl perl

# Creating user
RUN addgroup -S $APP_NAME; \
    adduser -Ss /bin/bash -h /home/$APP_NAME -G $APP_NAME $APP_NAME

WORKDIR /home/$APP_NAME

# .xauth/
#    conf/
#       application.conf
#    script/
#       keygen.sh
#    keys/
#       000...

# Copyng application files
RUN mkdir -p .$APP_NAME/{conf,keys,script}
ADD keygen.sh .$APP_NAME/script/
ADD $APP_ARCHIVE .
RUN unzip $APP_ARCHIVE
RUN mv $APP_NAME-$APP_VERSION $APP_NAME
RUN rm $APP_ARCHIVE

RUN chown -R $APP_NAME:$APP_NAME *

ENV CNF_DIRECTORY="/home/$APP_NAME/.$APP_NAME"
ENV APP_DIRECTORY="/home/$APP_NAME/$APP_NAME"

# Importing certificates
WORKDIR $APP_DIRECTORY

# Launching service
CMD $APP_DIRECTORY/bin/$APP_NAME \
    -Dconfig.file=$CNF_DIRECTORY/conf/application-$ENVIRONMENT.conf
