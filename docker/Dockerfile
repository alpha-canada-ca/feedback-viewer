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
RUN rm -rf /var/lib/apt/lists/*
RUN apt-get update && apt-get install -y wget && apt-get clean

WORKDIR /app

# Download AWS RDS CA bundle
RUN wget https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem \
    && keytool -import -alias aws-rds-global -file global-bundle.pem -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt \
    && rm global-bundle.pem

COPY --from=build /build/target/PageSuccess-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=3001
EXPOSE 3001

ENTRYPOINT ["java","-jar","app.jar"]
