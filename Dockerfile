FROM openjdk:17-jdk

WORKDIR /app

COPY build/libs/sp-media-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=release", "app.jar"]