# ===== Stage 1: Build the application =====
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Кэш зависимости
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходники
COPY . .

# Собираем проект
RUN mvn clean package -DskipTests

# ===== Stage 2: Run the application =====
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Копируем JAR из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh
