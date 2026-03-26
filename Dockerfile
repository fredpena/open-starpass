FROM eclipse-temurin:25-jre
COPY target/openstarpass.jar openstarpass.jar
ENV TZ=America/New_York
EXPOSE 15194
ENTRYPOINT ["java", "-jar", "/openstarpass.jar"]


