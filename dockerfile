#FROM maven:3.8.4-openjdk-11-slim
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
# Build the application
# RUN mvn clean package
# Run the application
# CMD ["java","-cp", "target/selenium-docker-crawl-java-1.0-SNAPSHOT-jar-with-dependencies.jar", "com.example.Main"]
#CMD ["java", "-jar", "target/*.jar"]

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/*SNAPSHOT.jar"]
