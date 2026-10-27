FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl app -am -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]