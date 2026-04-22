FROM mongo:4.0

LABEL version="1.0" \
      description="MongoDB container with authentication" \
      org.opencontainers.image.authors="enrico.russo.84@gmail.com"

ADD run.sh /run.sh
ADD set_mongodb_password.sh /set_mongodb_password.sh

RUN chmod +x /run.sh
RUN chmod +x /set_mongodb_password.sh

CMD ["/run.sh"]