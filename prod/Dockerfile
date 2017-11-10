FROM openjdk:8-jre-alpine
MAINTAINER Bart Hanssens <bart.hanssens@fedict.be>

RUN adduser -S dropwizard
COPY lod-query-1.0.jar /usr/local/bin/lod-query.jar
COPY query /home/dropwizard/
COPY config.yml /home/dropwizard
RUN chown -R dropwizard /home/dropwizard

USER dropwizard
WORKDIR /home/dropwizard

ENV JAVA_OPT=""
ENV DW_CFG=/home/dropwizard/config.yml

CMD java -server ${JAVA_OPT} -jar /usr/local/bin/lod-query.jar server ${DW_CFG}
 
EXPOSE 8080 8081
