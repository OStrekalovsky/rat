FROM maven:3.5.2-jdk-8-alpine AS MAVEN_TOOL_CHAIN
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
# Have to disable test because can't figure out how to disable main bean configuration in tests.
RUN mvn install -DskipTests

FROM openjdk:8-jre-alpine
COPY --from=MAVEN_TOOL_CHAIN /tmp/target/receipt-analyzer-*.jar /receipt-analyzer.jar
ENTRYPOINT ["/usr/bin/java", "-jar", "-Dspring.profiles.active=prod", "/receipt-analyzer.jar"]