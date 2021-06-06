FROM gradle:4.7.0-jdk8-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon 

FROM gcr.io/distroless/java  
COPY --from=build /usr/src/app/target/auth0.user.prov-1.0.jar /usr/app/auth0.user.prov-1.0.jar  
EXPOSE 8080  

ENTRYPOINT ["java","-jar","/usr/app/auth0.user.prov-1.0.jar"] 
 