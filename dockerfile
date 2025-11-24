#FROM maven:3.8.4-openjdk-11-slim
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
# Build the application
# RUN mvn clean package
# Run the application
# CMD ["java","-cp", "target/selenium-docker-crawl-java-1.0-SNAPSHOT-jar-with-dependencies.jar", "com.example.Main"]
#CMD ["java", "-jar", "target/*.jar"]

#FROM eclipse-temurin:17-jdk

#RUN apt-get update && apt-get install -y maven

#WORKDIR /app
#COPY . .

#RUN mvn -version
#RUN mvn clean package -DskipTests || true

# Print target folder so we can see if jar exists
#RUN ls -R .

#CMD ["sh", "-c", "ls target && java -jar target/*.jar"]

# Use the Eclipse Temurin base image with JDK 17
FROM eclipse-temurin:17-jdk

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory inside the container
WORKDIR /app

# Copy all the project files into the container
COPY . .

# Verify Maven is installed and check the version
RUN mvn -version

# Build the Spring Boot application, skipping tests
RUN mvn clean package -DskipTests

# Check the contents of the target directory to ensure the .jar file is built
RUN ls -l target/

# Set the entrypoint to run the application
CMD ["java", "-jar", "target/news-aggregator-1.0-SNAPSHOT.jar"]

