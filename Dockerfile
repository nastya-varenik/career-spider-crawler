# Этап 1: Сборка
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:17-jre
WORKDIR /app
# Копируем jar из папки target
COPY --from=build /app/target/*.jar app.jar

# ВАЖНО: Hugging Face требует порт 7860
ENV SERVER_PORT=7860
EXPOSE 7860

# Запускаем Spring Boot на порту 7860
ENTRYPOINT ["java", "-Dserver.port=7860", "-jar", "app.jar"]