FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY ecommerce-domain/pom.xml ecommerce-domain/pom.xml
COPY ecommerce-application/pom.xml ecommerce-application/pom.xml
COPY ecommerce-infrastructure/pom.xml ecommerce-infrastructure/pom.xml
COPY ecommerce-api/pom.xml ecommerce-api/pom.xml

COPY . .

RUN ./mvnw -pl ecommerce-api -am package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/ecommerce-api/target/ecommerce-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
