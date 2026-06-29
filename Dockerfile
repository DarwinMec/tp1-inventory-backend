# ====== ETAPA 1: Build ======
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copiar archivos Maven primero para aprovechar caché
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos al Maven Wrapper
RUN chmod +x mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar proyecto
RUN ./mvnw clean package -DskipTests


# ====== ETAPA 2: Runtime ======
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiar JAR generado
COPY --from=build /app/target/*.jar app.jar

# Puerto del backend
EXPOSE 8080

# Ejecutar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]