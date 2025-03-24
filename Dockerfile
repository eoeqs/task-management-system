#Build
FROM gradle:8.8-jdk17 as backend-build
WORKDIR /sources-build
COPY --chown=gradle:gradle . ./
RUN gradle build --no-daemon -x test

#Run
FROM openjdk:17
WORKDIR /application
COPY --from=backend-build /sources-build/build/libs/is_1-1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]