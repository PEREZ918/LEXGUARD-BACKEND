FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:resolve -B
COPY src/ src/
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV MYSQLHOST=monorail.proxy.rlwy.net
ENV MYSQLPORT=31215
ENV MYSQLDATABASE=railway
ENV MYSQLUSER=root
ENV MYSQLPASSWORD=meVnqCtwNMRlYtrErVPaRsjezagOLiKJ
ENV JWT_SECRET=TGV4R3VhcmRQcm9kU2VjcmV0S2V5MjAyNlByb2plY3RTZWN1cml0eUtleTEyMzQ1Ng==
ENTRYPOINT ["java", "-Xmx128m", "-XX:MaxMetaspaceSize=128m", "-Xss512k", "-XX:ReservedCodeCacheSize=64m", "-XX:CICompilerCount=2", "-jar", "app.jar"]
