FROM maven:3.5-jdk-8
RUN apt-get clean
RUN apt-get update
RUN mkdir -p /app
ADD target/PageSuccess-0.0.1-SNAPSHOT.jar /app/app.jar
#ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n
ENTRYPOINT ["java","-jar","/app/app.jar"]

