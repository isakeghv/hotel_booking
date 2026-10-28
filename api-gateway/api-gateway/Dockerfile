FROM amazoncorretto:25 AS build
RUN yum install -y tar gzip && rm -rf /var/cache/yum
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

FROM amazoncorretto:25-alpine-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]