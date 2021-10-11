FROM quay.io/quarkus/centos-quarkus-maven:21.0.0-java11 AS dependencies
COPY legume-service/pom.xml /usr/src/legume-service/
COPY legume-service-open-liberty/pom.xml /usr/src/legume-service-open-liberty/
COPY pom.xml /usr/src
RUN mvn -Dmaven.repo.local=.m2 -f /usr/src/pom.xml -f /usr/src/legume-service/pom.xml -f /usr/src/legume-service-open-liberty/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies


FROM quay.io/quarkus/centos-quarkus-maven:21.0.0-java11 as build-vegetables
COPY --from=dependencies /project/.m2 /usr/src/.m2
COPY legume-service/pom.xml /usr/src/legume-service/
COPY pom.xml /usr/src/pom.xml
COPY legume-service/src /usr/src/legume-service/src
USER root
RUN chown -R quarkus /usr/src/legume-service
USER quarkus
RUN mvn -Dmaven.repo.local=/usr/src/.m2 -DskipTests=true -f /usr/src/legume-service/pom.xml package

FROM maven:3.6.3-jdk-11-slim as build-vegetables-open-liberty
COPY --from=dependencies /project/.m2 /usr/src/.m2
COPY legume-service-open-liberty/pom.xml /usr/src/legume-service-open-liberty/
COPY pom.xml /usr/src/pom.xml
COPY legume-service-open-liberty/src /usr/src/legume-service-open-liberty/src
USER root
#RUN chown -R quarkus /usr/src/legume-service-open-liberty
#USER quarkus
RUN mvn -Dmaven.repo.local=/usr/src/.m2 -DskipTests=true -f /usr/src/legume-service-open-liberty/pom.xml package


# Building with dependendencies all contained in Docker: docker build --target vegetables .
FROM openjdk:11.0.6-jre-slim-buster as vegetables
COPY --from=build-vegetables /usr/src/legume-service/target/quarkus-app /work/
RUN chmod 775 /work/quarkus-run.jar
EXPOSE 8080
CMD ["java", "-jar", "/work/quarkus-run.jar"]

# Building with dependendencies all contained in Docker: docker build --target vegetables .
FROM openliberty/open-liberty:full-java11-openj9-ubi as vegetables-open-liberty
COPY --chown=1001:0 --from=build-vegetables-open-liberty /usr/src/legume-service-open-liberty/src/main/liberty/config/server.xml /config/
COPY --chown=1001:0 --from=build-vegetables-open-liberty /usr/src/legume-service-open-liberty/target/legume-service-open-liberty.war /config/apps/legume-service-open-liberty.war
RUN mkdir -p /opt/ol/wlp/usr/shared/config/lib/global
COPY --chown=1001:0 --from=build-vegetables-open-liberty /usr/src/legume-service-open-liberty/target/legume-service-open-liberty/WEB-INF/lib/postgresql-42.2.23.jar /opt/ol/wlp/usr/shared/config/lib/global/postgresql-42.2.23.jar
EXPOSE 8080
RUN configure.sh


# Build with dependencies locally for dev docker build --target vegetablesdev .
FROM openjdk:11.0.6-jre-slim-buster as vegetablesdev
COPY legume-service/target/quarkus-app/ /work/
RUN chmod 775 /work
EXPOSE 8080
CMD ["java", "-jar", "/work/quarkus-run.jar"]
