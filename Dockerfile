# Builds frontend and backend into one image (one container, one command).

# ---------- Stage 1: build the Vue frontend ----------
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
# Dependencies first: this step is only repeated when package.json or package-lock.json change.
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---------- Stage 2: build the Spring Boot backend with the frontend inside ----------
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app/backend
# Dependencies first, for the same reason as above.
COPY backend/pom.xml ./
RUN mvn -q dependency:go-offline
COPY backend/src ./src
# The built frontend becomes static content that Spring Boot serves.
COPY --from=frontend /app/frontend/dist ./src/main/resources/static
# Tests use Testcontainers and need Docker, which is not available inside a Docker build.
# They run before building the image, with ./mvnw test.
RUN mvn -q package -DskipTests

# ---------- Stage 3: only what is needed to run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
# Run as an unprivileged user instead of root.
RUN useradd --system --create-home kino
COPY --from=backend /app/backend/target/*.jar app.jar
USER kino
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]