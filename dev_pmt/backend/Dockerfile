# Étape de compilation Maven avec Java 17.
FROM maven:3.9-eclipse-temurin-17 AS build

# Le projet est construit dans un dossier de travail dédié.
WORKDIR /app

# Les dépendances sont téléchargées dans une couche réutilisable.
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Le code est copié après les dépendances pour conserver le cache Docker.
COPY src/ src/
RUN mvn clean package -DskipTests -B

# Étape d'exécution légère avec Java 17.
FROM eclipse-temurin:17-jre

# Le service écoute sur le port HTTP du backend.
WORKDIR /app
EXPOSE 8080

# L'application ne doit pas être exécutée avec les privilèges root.
RUN useradd --system --create-home --uid 10001 pmt
COPY --from=build /app/target/pmtbackend-0.0.1-SNAPSHOT.jar app.jar
RUN chown pmt:pmt app.jar
USER pmt

# Le JAR contient l'application complète et démarre le serveur API.
ENTRYPOINT ["java", "-jar", "app.jar"]
