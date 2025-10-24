FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
ENV TZ=UTC \
    SPRING_PROFILES_ACTIVE=docker \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
