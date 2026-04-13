# Шаг 1: Сборка проекта
FROM maven:3.9.6-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Шаг 2: Запуск приложения
FROM eclipse-temurin:17-jre
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]