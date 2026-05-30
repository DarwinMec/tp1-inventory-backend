# ====== ETAPA 1: Build ======
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Compilar el proyecto con Maven Wrapper incluido en tu backend
RUN ./mvnw clean package -DskipTests


# ====== ETAPA 2: Runtime ======
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el JAR construido desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto donde corre tu backend
EXPOSE 8080

# Ejecutar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
