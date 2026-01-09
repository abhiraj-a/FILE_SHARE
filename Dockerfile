FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy Maven wrapper & config
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Fix permission for Linux
RUN chmod +x mvnw

# Cache dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build and rename jar → app.jar
RUN ./mvnw clean package -DskipTests && \
    cp target/*.jar target/app.jar

EXPOSE 8080

CMD ["java", "-jar", "target/app.jar"]
