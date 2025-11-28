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
RUN apt-get update && apt-get clean

WORKDIR /app
COPY --from=build /build/target/PageSuccess-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=3001
EXPOSE 3001

ENTRYPOINT ["java","-jar","app.jar"]
