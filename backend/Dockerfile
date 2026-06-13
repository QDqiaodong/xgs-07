ARG DOCKER_REGISTRY

FROM ${DOCKER_REGISTRY}maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .

RUN mkdir -p src/main/java && \
    printf '%s\n' 'public class DockerCacheWarmup { public static void main(String[] args) {} }' > src/main/java/DockerCacheWarmup.java && \
    mvn package -Dmaven.test.skip=true -B && \
    rm -rf src target

COPY src ./src

RUN mvn package -Dmaven.test.skip=true -B

FROM ${DOCKER_REGISTRY}eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9035

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
