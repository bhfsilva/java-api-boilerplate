FROM maven:3.9.9-amazoncorretto-23 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests=true
FROM openjdk:26-rc-slim
WORKDIR /app
COPY --from=build /app/target/boilerplate-app.jar /app/boilerplate-app.jar
ENV TZ="America/Sao_Paulo"
CMD ["java", "-jar", "boilerplate-app.jar"]
