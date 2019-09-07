# work in progress
FROM maven:3-jdk-8
RUN apt-get update
RUN apt-get -y install systemd
RUN apt-get -y install redis-server
RUN systemctl enable redis-server.service
RUN mkdir -p /app
WORKDIR /app
COPY pom.xml .
EXPOSE 8080
ENTRYPOINT ["mvn", "verify"]