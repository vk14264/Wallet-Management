FROM eclipse-temurin:17-jdk-jammy
VOLUME /tmp
COPY target/wallet-management-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
