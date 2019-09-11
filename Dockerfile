# work in progress
FROM maven:3-jdk-8
RUN mkdir /app
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . ./
EXPOSE 8081