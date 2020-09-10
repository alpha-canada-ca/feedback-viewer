FROM maven:3.5-jdk-8
RUN apt-get clean
RUN apt-get update
RUN yes | apt-get install python
RUN yes | apt-get install python3-pandas
RUN yes | apt-get install python3-matplotlib
RUN yes | apt-get install python3-nltk
RUN yes | apt-get install python3-pip
RUN pip3 --version
RUN yes | pip3 install wordcloud
COPY python/ /tmp/
RUN mkdir -p /build
WORKDIR /build
COPY pom.xml /build
#Download all required dependencies into one layer
#RUN mvn -B dependency:resolve dependency:resolve-plugins
#Copy source code
COPY src /build/src
# Build application
#RUN mvn package
RUN mkdir -p /app
ADD target/PageSuccess-0.0.1-SNAPSHOT.jar /app/app.jar
ADD python/ /app/python
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n
ENTRYPOINT ["java","-jar","/app/app.jar"]

