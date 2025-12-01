# Build stage
FROM maven:3.8.3-jdk-8 AS build
WORKDIR /build

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:8-jre-jammy

WORKDIR /app

# Install wget and clean up in single layer
RUN apt-get update && \
    apt-get install -y wget ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Download and import AWS RDS CA bundle for DocumentDB
RUN wget https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem -O /tmp/rds-combined-ca-bundle.pem && \
    keytool -import \
    -trustcacerts \
    -alias aws-rds-ca-bundle \
    -file /tmp/rds-combined-ca-bundle.pem \
    -keystore ${JAVA_HOME}/lib/security/cacerts \
    -storepass changeit \
    -noprompt && \
    rm /tmp/rds-combined-ca-bundle.pem

# Verify the certificate was imported
RUN keytool -list -keystore ${JAVA_HOME}/lib/security/cacerts -storepass changeit -alias aws-rds-ca-bundle

COPY --from=build /build/target/PageSuccess-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=3001
EXPOSE 3001

ENTRYPOINT ["java","-jar","app.jar"]