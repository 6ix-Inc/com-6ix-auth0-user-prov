FROM gradle:7.0.2-jdk8 AS build
COPY --chown=gradle:gradle . /usr/app
WORKDIR /usr/app
RUN gradle build --no-daemon 

FROM gcr.io/distroless/java  
COPY --from=build /usr/app/build/libs/com-6ix-auth0-user-prov-1.0.jar /usr/app/com-6ix-auth0-user-prov-1.0.jar  
EXPOSE 8080  

ENTRYPOINT ["java","-jar","/usr/app/com-6ix-auth0-user-prov-1.0.jar"] 
 