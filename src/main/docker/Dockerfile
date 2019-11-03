FROM openjdk:8

MAINTAINER Holger Bruch <holger.bruch@mitfahrdezentrale.de>

RUN mkdir /osmroutefix \
	&& cd /osmroutefix
	
WORKDIR /osmroutefix

# Expose Web Port
EXPOSE 8989 8990

# To mount external folder supply -v /path/on/host:/osmroutefix/data to docker run
VOLUME /osmroutefix/data

COPY docker-entrypoint.sh /osmroutefix/docker-entrypoint.sh
COPY config.yml /osmroutefix/config.yml
RUN chmod ugo+x /osmroutefix/docker-entrypoint.sh

COPY maven/ /osmroutefix/
ENV osmroutefix_VERSION=@docker.project.version@

ENTRYPOINT ["/osmroutefix/docker-entrypoint.sh"]

CMD ["osmroutefix"]
