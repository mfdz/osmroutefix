FROM openjdk:8-jdk

ENV JAVA_OPTS "-server -Xconcurrentio -Xmx1g -Xms1g -XX:+UseG1GC -Ddw.server.applicationConnectors[0].bindHost=0.0.0.0 -Ddw.server.applicationConnectors[0].port=8989"

RUN mkdir -p /data && mkdir -p /osmroutefix

# install node - only required for JS UI
RUN apt-get install -y wget \
       && curl -sL https://deb.nodesource.com/setup_11.x | bash - \
       && apt-get install -y nodejs

COPY . /osmroutefix/

WORKDIR /osmroutefix

# create main.js - only required for JS UI
RUN npm install && npm run bundleProduction && cd ..

RUN ./osmroutefix.sh build

VOLUME [ "/data" ]

EXPOSE 8989

ENTRYPOINT [ "./osmroutefix.sh", "web" ]

CMD [ "/data/europe_germany_berlin.pbf" ]
