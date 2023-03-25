FROM gradle:8.0.2-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Build the fat JAR (if not fat then fails to run dependencies):
RUN gradle shadowJar 


FROM openjdk:17-oracle

# TODO REMOVE THIS SHIT ---!!!!!!!!!!
RUN mkdir /app

# Copy over the fat JAR:
COPY --from=build /home/gradle/src/app/build/libs/app-all.jar /app/app.jar

# Entrypoint is the fat JAR:
ENTRYPOINT ["java","-jar","/app/app.jar"]